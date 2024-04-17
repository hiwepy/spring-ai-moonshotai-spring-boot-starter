package org.springframework.ai.moonshotai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.moonshotai.api.ApiUtils;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.ai.moonshotai.api.MoonshotAiChatOptions;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MoonshotAiChatClient
        extends AbstractFunctionCallSupport<MoonshotAiApi.ChatCompletionMessage, MoonshotAiApi.ChatCompletionRequest, ResponseEntity<MoonshotAiApi.ChatCompletion>>
        implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private MoonshotAiChatOptions defaultOptions;
    /**
     * Low-level 智普 API library.
     */
    private final MoonshotAiApi moonshotAiApi;
    private final RetryTemplate retryTemplate;

    public MoonshotAiChatClient(MoonshotAiApi moonshotAiApi) {
        this(moonshotAiApi, MoonshotAiChatOptions.builder()
                    .withModel(MoonshotAiApi.ChatModel.MOONSHOT_V1_8K.getValue())
                    .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                    .withTopP(ApiUtils.DEFAULT_TOP_P)
                    .build());
    }

    public MoonshotAiChatClient(MoonshotAiApi moonshotAiApi, MoonshotAiChatOptions options) {
        this(moonshotAiApi, options, null, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public MoonshotAiChatClient(MoonshotAiApi moonshotAiApi, MoonshotAiChatOptions options,
                                FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
        super(functionCallbackContext);
        Assert.notNull(moonshotAiApi, "MoonshotAiApi must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.moonshotAiApi = moonshotAiApi;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }


    @Override
    public ChatResponse call(Prompt prompt) {

        var request = createRequest(prompt, false);

        return retryTemplate.execute(ctx -> {

            ResponseEntity<MoonshotAiApi.ChatCompletion> completionEntity = this.callWithFunctionSupport(request);

            var chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            List<Generation> generations = chatCompletion.choices()
                    .stream()
                    .map(choice -> new Generation(choice.message().content(), toMap(chatCompletion.id(), choice))
                            .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null)))
                    .toList();

            return new ChatResponse(generations);
        });
    }

    private Map<String, Object> toMap(String id, MoonshotAiApi.ChatCompletion.Choice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.message();
        if (message.role() != null) {
            map.put("role", message.role().name());
        }
        if (choice.finishReason() != null) {
            map.put("finishReason", choice.finishReason().name());
        }
        map.put("id", id);
        return map;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        var request = createRequest(prompt, true);

        return retryTemplate.execute(ctx -> {

            var completionChunks = this.moonshotAiApi.chatCompletionStream(request);

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            return completionChunks.map(chunk -> toChatCompletion(chunk)).map(chatCompletion -> {

                chatCompletion = handleFunctionCallOrReturn(request, ResponseEntity.of(Optional.of(chatCompletion)))
                        .getBody();

                @SuppressWarnings("null")
                String id = chatCompletion.id();

                List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                    if (choice.message().role() != null) {
                        roleMap.putIfAbsent(id, choice.message().role().name());
                    }
                    String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                    var generation = new Generation(choice.message().content(),
                            Map.of("id", id, "role", roleMap.get(id), "finishReason", finish));
                    if (choice.finishReason() != null) {
                        generation = generation
                                .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                    }
                    return generation;
                }).toList();
                return new ChatResponse(generations);
            });
        });
    }

    private MoonshotAiApi.ChatCompletion toChatCompletion(MoonshotAiApi.ChatCompletionChunk chunk) {
        List<MoonshotAiApi.ChatCompletion.Choice> choices = chunk.choices()
                .stream()
                .map(cc -> new MoonshotAiApi.ChatCompletion.Choice(cc.index(), cc.delta(), cc.finishReason()))
                .toList();

        return new MoonshotAiApi.ChatCompletion(chunk.id(), "chat.completion", chunk.created(), chunk.model(), choices, null);
    }

    /**
     * Accessible for testing.
     */
    MoonshotAiApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .map(m -> new MoonshotAiApi.ChatCompletionMessage(m.getContent(),
                        MoonshotAiApi.ChatCompletionMessage.Role.valueOf(m.getMessageType().name())))
                .toList();

        var request = new MoonshotAiApi.ChatCompletionRequest(null, chatCompletionMessages, stream);

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, MoonshotAiApi.ChatCompletionRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        MoonshotAiChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request,
                        MoonshotAiApi.ChatCompletionRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    MoonshotAiChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, MoonshotAiApi.ChatCompletionRequest.class);
        }

        return request;
    }

    private List<MoonshotAiApi.FunctionTool> getFunctionTools(Set<String> functionNames) {
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new MoonshotAiApi.FunctionTool.Function(functionCallback.getDescription(),
                    functionCallback.getName(), functionCallback.getInputTypeSchema());
            return new MoonshotAiApi.FunctionTool(function);
        }).toList();
    }

    //
    // Function Calling Support
    //
    @Override
    protected MoonshotAiApi.ChatCompletionRequest doCreateToolResponseRequest(MoonshotAiApi.ChatCompletionRequest previousRequest,
                                                                              MoonshotAiApi.ChatCompletionMessage responseMessage,
                                                                              List<MoonshotAiApi.ChatCompletionMessage> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (MoonshotAiApi.ChatCompletionMessage.ToolCall toolCall : responseMessage.toolCalls()) {

            var functionName = toolCall.function().name();
            String functionArguments = toolCall.function().arguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory
                    .add(new MoonshotAiApi.ChatCompletionMessage(functionResponse, MoonshotAiApi.ChatCompletionMessage.Role.TOOL, functionName, null));
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        MoonshotAiApi.ChatCompletionRequest newRequest = new MoonshotAiApi.ChatCompletionRequest(previousRequest.requestId(), conversationHistory, false);
        newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, MoonshotAiApi.ChatCompletionRequest.class);

        return newRequest;
    }

    @Override
    protected List<MoonshotAiApi.ChatCompletionMessage> doGetUserMessages(MoonshotAiApi.ChatCompletionRequest request) {
        return request.messages();
    }

    @SuppressWarnings("null")
    @Override
    protected MoonshotAiApi.ChatCompletionMessage doGetToolResponseMessage(ResponseEntity<MoonshotAiApi.ChatCompletion> chatCompletion) {
        return chatCompletion.getBody().choices().iterator().next().message();
    }

    @Override
    protected ResponseEntity<MoonshotAiApi.ChatCompletion> doChatCompletion(MoonshotAiApi.ChatCompletionRequest request) {
        return this.moonshotAiApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<MoonshotAiApi.ChatCompletion> chatCompletion) {

        var body = chatCompletion.getBody();
        if (body == null) {
            return false;
        }

        var choices = body.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        return !CollectionUtils.isEmpty(choices.get(0).message().toolCalls());
    }
}
