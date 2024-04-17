package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.moonshotai.api.ApiUtils;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.ai.moonshotai.api.MoonshotAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MoonshotAiChatProperties.CONFIG_PREFIX)
public class MoonshotAiChatProperties extends MoonshotAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.moonshotai.chat";

    /**
     * Enable Moonshot AI chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever Moonshot AI options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private MoonshotAiChatOptions options = MoonshotAiChatOptions.builder()
            .withModel(MoonshotAiApi.ChatModel.MOONSHOT_V1_8K.getValue())
            .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
            .withTopP(ApiUtils.DEFAULT_TOP_P)
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
