package org.springframework.ai.moonshotai.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MoonshotAiFileApi {

    private final RestClient restClient;

    /**
     * Create a new Moonshot AI File api with base URL set to https://api.moonshot.cn
     * @param apiKey Moonshot AI apiKey.
     */
    public MoonshotAiFileApi(String apiKey) {
        this(ApiUtils.DEFAULT_BASE_URL, apiKey, RestClient.builder());
    }

    /**
     * Create a new Moonshot AI File API with the provided base URL.
     * @param baseUrl the base URL for the Moonshot AI API.
     * @param apiKey Moonshot AI apiKey.
     * @param restClientBuilder the rest client builder to use.
     */
    public MoonshotAiFileApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder) {
        this(baseUrl, apiKey, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
    }

    /**
     * Create a new Moonshot AI File API with the provided base URL.
     * @param baseUrl the base URL for the Moonshot AI API.
     * @param apiKey Moonshot AI apiKey.
     * @param restClientBuilder the rest client builder to use.
     * @param responseErrorHandler the response error handler to use.
     */
    public MoonshotAiFileApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder,
                             ResponseErrorHandler responseErrorHandler) {

        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(apiKey))
                .defaultStatusHandler(responseErrorHandler)
                .build();
    }

    // @formatter:off
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MoonshotAiFileRequest (
            @JsonProperty("file") File file) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MoonshotAiFileResponse(
            @JsonProperty("object") String object,
            @JsonProperty("data") List<Data> data) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Data(
                @JsonProperty("id") String id,
                @JsonProperty("object") String object,
                @JsonProperty("bytes") Long bytes,
                @JsonProperty("created_at") Long created,
                @JsonProperty("filename") String filename,
                @JsonProperty("purpose") String purpose,
                @JsonProperty("status") String status,
                @JsonProperty("status_details") String statusDetails) {
        }
    }



    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MoonshotAiFilDeleteResponse(
            @JsonProperty("deleted") Boolean deleted,
            @JsonProperty("id") String id,
            @JsonProperty("object") String object) {
    }

    // @formatter:onn

    /**
     * 列举出用户已上传的所有文件
     * @return
     */
    public ResponseEntity<MoonshotAiFileResponse> listFile() {
        return this.restClient.get()
                .uri("/v1/files")
                .retrieve()
                .toEntity(MoonshotAiFileResponse.class);
    }

    /**
     * 上传文件
     * 注意，单个用户最多只能上传 1000 个文件，单文件不超过 100MB，同时所有已上传的文件总和不超过 10G 容量。如果您要抽取更多文件，需要先删除一部分不再需要的文件。
     * @param file
     * @return
     */
    public ResponseEntity<MoonshotAiFileResponse.Data> uploadFile(MultipartFile file) throws IOException {

        Assert.notNull(file, "File cannot be null.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(file.getOriginalFilename())
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return this.restClient.post()
                .uri("/v1/files")
                .body(requestEntity)
                .retrieve()
                .toEntity(MoonshotAiFileResponse.Data.class);
    }

    /**
     * 删除文件
     * 本功能可以用于删除不再需要使用的文件
     * @param fileId 文件 ID
     * @return {
     *     "deleted": true,
     *     "id": "cofk82hhmfr6003nje5g",
     *     "object": "file"
     * }
     */
    public ResponseEntity<MoonshotAiFilDeleteResponse> deleteFile(String fileId) {
        Assert.hasLength(fileId, "File ID cannot be empty.");
        return this.restClient.delete()
                .uri("/v1/files/%s".formatted(fileId))
                .retrieve()
                .toEntity(MoonshotAiFilDeleteResponse.class);
    }

}
