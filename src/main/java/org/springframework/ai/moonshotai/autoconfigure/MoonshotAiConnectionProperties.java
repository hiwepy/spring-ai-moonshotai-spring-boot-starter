package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.moonshotai.api.ApiUtils;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MoonshotAiConnectionProperties.CONFIG_PREFIX)
public class MoonshotAiConnectionProperties extends MoonshotAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.moonshotai";

}
