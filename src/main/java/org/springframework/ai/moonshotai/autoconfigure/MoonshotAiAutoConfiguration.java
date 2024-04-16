package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.moonshotai.MoonshotAiChatClient;
import org.springframework.ai.moonshotai.MoonshotAiEmbeddingClient;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
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
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Moonshot AI Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ MoonshotAiChatProperties.class, MoonshotAiConnectionProperties.class, MoonshotAiEmbeddingProperties.class })
@ConditionalOnClass(MoonshotAiApi.class)
public class MoonshotAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MoonshotAiApi zhipuAiApi(MoonshotAiConnectionProperties properties, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

        Assert.hasText(properties.getApiKey(), "ZhipuAI API key must be set");
        Assert.hasText(properties.getBaseUrl(), "ZhipuAI base URL must be set");

        return new MoonshotAiApi(properties.getBaseUrl(), properties.getApiKey(), restClientBuilder, responseErrorHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MoonshotAiChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public MoonshotAiChatClient zhipuAiChatClient(MoonshotAiApi moonshotAiApi,
                                                  MoonshotAiChatProperties chatProperties,
                                                  List<FunctionCallback> toolFunctionCallbacks,
                                                  FunctionCallbackContext functionCallbackContext,
                                                  RetryTemplate retryTemplate) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new MoonshotAiChatClient(moonshotAiApi, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public MoonshotAiEmbeddingClient zhipuAiEmbeddingClient(MoonshotAiApi moonshotAiApi,
                                                            MoonshotAiEmbeddingProperties embeddingProperties,
                                                            RetryTemplate retryTemplate) {

        return new MoonshotAiEmbeddingClient(moonshotAiApi, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
