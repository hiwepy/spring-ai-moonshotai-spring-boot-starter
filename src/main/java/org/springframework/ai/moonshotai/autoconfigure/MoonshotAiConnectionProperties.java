package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MoonshotAiConnectionProperties.CONFIG_PREFIX)
public class MoonshotAiConnectionProperties {

    public static final String CONFIG_PREFIX = "spring.ai.moonshotai";

    /**
     * Base URL where Moonshot AI API server is running.
     */
    private String baseUrl = MoonshotAiApi.DEFAULT_BASE_URL;

    private String apiKey;

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
