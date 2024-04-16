package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.ai.moonshotai.api.MoonshotAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MoonshotAiChatProperties.CONFIG_PREFIX)
public class MoonshotAiChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.moonshotai.chat";

    public static final String DEFAULT_CHAT_MODEL = MoonshotAiApi.ChatModel.GLM_3_TURBO.getValue();

    private static final Float DEFAULT_TEMPERATURE = 0.95f;

    private static final Float DEFAULT_TOP_P = 1.0f;

    /**
     * Enable ZhipuAi chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever ZhipuAi options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private MoonshotAiChatOptions options = MoonshotAiChatOptions.builder()
            .withModel(DEFAULT_CHAT_MODEL)
            .withTemperature(DEFAULT_TEMPERATURE)
            .withTopP(DEFAULT_TOP_P)
            .build();

    public MoonshotAiChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(MoonshotAiChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
