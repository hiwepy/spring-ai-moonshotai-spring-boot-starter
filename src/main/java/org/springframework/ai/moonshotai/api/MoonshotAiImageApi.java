package org.springframework.ai.moonshotai.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

public class MoonshotAiImageApi {

    public static final String DEFAULT_IMAGE_MODEL = ImageModel.DALL_E_3.getValue();

    private final RestClient restClient;

    /**
     * Create a new Moonshot AI Image api with base URL set to https://api.moonshot.cn
     * @param apiKey Moonshot AI apiKey.
     */
    public MoonshotAiImageApi(String apiKey) {
        this(ApiUtils.DEFAULT_BASE_URL, apiKey, RestClient.builder());
    }

    /**
     * Create a new Moonshot AI Image API with the provided base URL.
     * @param baseUrl the base URL for the Moonshot AI API.
     * @param apiKey Moonshot AI apiKey.
     * @param restClientBuilder the rest client builder to use.
     */
    public MoonshotAiImageApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder) {
        this(baseUrl, apiKey, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
    }

    /**
     * Create a new Moonshot AI Image API with the provided base URL.
     * @param baseUrl the base URL for the Moonshot AI API.
     * @param apiKey Moonshot AI apiKey.
     * @param restClientBuilder the rest client builder to use.
     * @param responseErrorHandler the response error handler to use.
     */
    public MoonshotAiImageApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder,
                          ResponseErrorHandler responseErrorHandler) {

        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(apiKey))
                .defaultStatusHandler(responseErrorHandler)
                .build();
    }

    /**
     * Moonshot AI Image API model.
     * <a href="https://platform.MoonshotAi.com/docs/models/dall-e">DALL·E</a>
     */
    public enum ImageModel {

        /**
         * The latest DALL·E model released in Nov 2023.
         */
        DALL_E_3("dall-e-3"),

        /**
         * The previous DALL·E model released in Nov 2022. The 2nd iteration of DALL·E
         * with more realistic, accurate, and 4x greater resolution images than the
         * original model.
         */
        DALL_E_2("dall-e-2");

        private final String value;

        ImageModel(String model) {
            this.value = model;
        }

        public String getValue() {
            return this.value;
        }

    }

    // @formatter:off
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MoonshotAiImageRequest (
            @JsonProperty("prompt") String prompt,
            @JsonProperty("model") String model,
            @JsonProperty("n") Integer n,
            @JsonProperty("quality") String quality,
            @JsonProperty("response_format") String responseFormat,
            @JsonProperty("size") String size,
            @JsonProperty("style") String style,
            @JsonProperty("user") String user) {

        public MoonshotAiImageRequest(String prompt, String model) {
            this(prompt, model, null, null, null, null, null, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MoonshotAiImageResponse(
            @JsonProperty("created") Long created,
            @JsonProperty("data") List<Data> data) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Data(
            @JsonProperty("url") String url,
            @JsonProperty("b64_json") String b64Json,
            @JsonProperty("revised_prompt") String revisedPrompt) {
    }
    // @formatter:onn

    public ResponseEntity<MoonshotAiImageResponse> createImage(MoonshotAiImageRequest moonshotAiImageRequest) {
        Assert.notNull(moonshotAiImageRequest, "Image request cannot be null.");
        Assert.hasLength(moonshotAiImageRequest.prompt(), "Prompt cannot be empty.");

        return this.restClient.post()
                .uri("v1/images/generations")
                .body(moonshotAiImageRequest)
                .retrieve()
                .toEntity(MoonshotAiImageResponse.class);
    }

}
