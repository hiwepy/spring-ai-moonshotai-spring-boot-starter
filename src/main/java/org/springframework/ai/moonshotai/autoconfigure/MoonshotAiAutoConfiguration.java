package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.moonshotai.MoonshotAiChatClient;
import org.springframework.ai.moonshotai.MoonshotAiEmbeddingClient;
import org.springframework.ai.moonshotai.MoonshotAiImageClient;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.ai.moonshotai.api.MoonshotAiImageApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Moonshot AI Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ MoonshotAiChatProperties.class, MoonshotAiConnectionProperties.class, MoonshotAiEmbeddingProperties.class, MoonshotAiImageProperties.class })
@ConditionalOnClass(MoonshotAiApi.class)
public class MoonshotAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MoonshotAiChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public MoonshotAiChatClient moonshotAiChatClient(MoonshotAiConnectionProperties commonProperties,
                                                     MoonshotAiChatProperties chatProperties,
                                                     List<FunctionCallback> toolFunctionCallbacks,
                                                     FunctionCallbackContext functionCallbackContext,
                                                     RestClient.Builder restClientBuilder,
                                                     ResponseErrorHandler responseErrorHandler,
                                                     RetryTemplate retryTemplate) {

        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }

        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();

        Assert.hasText(apiKey, "Moonshot AI API key must be set");
        Assert.hasText(baseUrl, "Moonshot AI base URL must be set");

        var moonshotAiApi = new MoonshotAiApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);

        return new MoonshotAiChatClient(moonshotAiApi, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public MoonshotAiEmbeddingClient moonshotAiEmbeddingClient(MoonshotAiConnectionProperties commonProperties,
                                                               MoonshotAiEmbeddingProperties embeddingProperties,
                                                               RestClient.Builder restClientBuilder,
                                                               ResponseErrorHandler responseErrorHandler,
                                                               RetryTemplate retryTemplate) {

        String apiKey = StringUtils.hasText(embeddingProperties.getApiKey()) ? embeddingProperties.getApiKey() : commonProperties.getApiKey();
        String baseUrl = StringUtils.hasText(embeddingProperties.getBaseUrl()) ? embeddingProperties.getBaseUrl() : commonProperties.getBaseUrl();

        Assert.hasText(apiKey, "Moonshot AI API key must be set");
        Assert.hasText(baseUrl, "Moonshot AI base URL must be set");

        var moonshotAiApi = new MoonshotAiApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);

        return new MoonshotAiEmbeddingClient(moonshotAiApi, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MoonshotAiImageProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public MoonshotAiImageClient moonshotAiImageClient(MoonshotAiConnectionProperties commonProperties,
                                                       MoonshotAiImageProperties imageProperties,
                                                       RestClient.Builder restClientBuilder,
                                                       ResponseErrorHandler responseErrorHandler,
                                                       RetryTemplate retryTemplate) {

        String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey() : commonProperties.getApiKey();
        String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl() : commonProperties.getBaseUrl();

        Assert.hasText(apiKey, "Moonshot AI API key must be set");
        Assert.hasText(baseUrl, "Moonshot AI base URL must be set");

        var moonshotAiImageApi = new MoonshotAiImageApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);

        return new MoonshotAiImageClient(moonshotAiImageApi, imageProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
