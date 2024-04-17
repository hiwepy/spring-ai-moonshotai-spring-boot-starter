package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.moonshotai.api.MoonshotAiImageOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MoonshotAiImageProperties.CONFIG_PREFIX)
public class MoonshotAiImageProperties extends MoonshotAiParentProperties{

    public static final String CONFIG_PREFIX = "spring.ai.moonshotai.image";

    /**
     * Enable Moonshot AI Image client.
     */
    private boolean enabled = true;

    /**
     * Options for Moonshot AI Image API.
     */
    @NestedConfigurationProperty
    private MoonshotAiImageOptions options = MoonshotAiImageOptions.builder().build();

    public MoonshotAiImageOptions getOptions() {
        return options;
    }

    public void setOptions(MoonshotAiImageOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
