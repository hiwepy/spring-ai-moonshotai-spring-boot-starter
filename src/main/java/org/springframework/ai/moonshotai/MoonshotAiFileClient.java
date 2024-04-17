package org.springframework.ai.moonshotai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.moonshotai.api.MoonshotAiFileApi;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MoonshotAiFileClient {

    private final static Logger logger = LoggerFactory.getLogger(MoonshotAiFileClient.class);

    private final MoonshotAiFileApi moonshotAiFileApi;

    public final RetryTemplate retryTemplate;

    public MoonshotAiFileClient(MoonshotAiFileApi moonshotAiFileApi, RetryTemplate retryTemplate) {
        Assert.notNull(moonshotAiFileApi, "MoonshotAiFileApi must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.moonshotAiFileApi = moonshotAiFileApi;
        this.retryTemplate = retryTemplate;
    }

    public ResponseEntity<MoonshotAiFileApi.MoonshotAiFileResponse> listFile() {
        return retryTemplate.execute(context -> {
            logger.debug("Listing files");
            return moonshotAiFileApi.listFile();
        });
    }

    public ResponseEntity<MoonshotAiFileApi.MoonshotAiFileResponse.Data> uploadFile(MultipartFile file) throws IOException {
        return retryTemplate.execute(context -> {
            logger.debug("Uploading file");
            return moonshotAiFileApi.uploadFile(file);
        });
    }

    public ResponseEntity<MoonshotAiFileApi.MoonshotAiFilDeleteResponse> deleteFile(String fileId) {
        return retryTemplate.execute(context -> {
            logger.debug("Deleting file");
            return moonshotAiFileApi.deleteFile(fileId);
        });
    }

}
