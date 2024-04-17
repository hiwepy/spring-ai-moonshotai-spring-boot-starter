package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.moonshotai.api.ApiUtils;

class MoonshotAiParentProperties {

    /**
     * Base URL where Moonshot AI API server is running.
     */
    private String baseUrl = ApiUtils.DEFAULT_BASE_URL;

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
