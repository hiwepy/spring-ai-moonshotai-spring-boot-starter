package org.springframework.ai.moonshotai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.moonshotai.api.MoonshotAiImageApi;
import org.springframework.ai.moonshotai.api.MoonshotAiImageOptions;
import org.springframework.ai.moonshotai.metadata.MoonshotAiImageGenerationMetadata;
import org.springframework.ai.moonshotai.metadata.MoonshotAiImageResponseMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

public class MoonshotAiImageClient implements ImageClient {

    private final static Logger logger = LoggerFactory.getLogger(MoonshotAiImageClient.class);

    private MoonshotAiImageOptions defaultOptions;

    private final MoonshotAiImageApi moonshotAiImageApi;

    public final RetryTemplate retryTemplate;

    public MoonshotAiImageClient(MoonshotAiImageApi moonshotAiImageApi) {
        this(moonshotAiImageApi, MoonshotAiImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public MoonshotAiImageClient(MoonshotAiImageApi moonshotAiImageApi, MoonshotAiImageOptions defaultOptions,
                             RetryTemplate retryTemplate) {
        Assert.notNull(moonshotAiImageApi, "MoonshotAiImageApi must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.moonshotAiImageApi = moonshotAiImageApi;
        this.defaultOptions = defaultOptions;
        this.retryTemplate = retryTemplate;
    }

    public MoonshotAiImageOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {
        return this.retryTemplate.execute(ctx -> {

            String instructions = imagePrompt.getInstructions().get(0).getText();

            MoonshotAiImageApi.MoonshotAiImageRequest imageRequest = new MoonshotAiImageApi.MoonshotAiImageRequest(instructions,
                    MoonshotAiImageApi.DEFAULT_IMAGE_MODEL);

            if (this.defaultOptions != null) {
                imageRequest = ModelOptionsUtils.merge(this.defaultOptions, imageRequest,
                        MoonshotAiImageApi.MoonshotAiImageRequest.class);
            }

            if (imagePrompt.getOptions() != null) {
                imageRequest = ModelOptionsUtils.merge(toMoonshotAiImageOptions(imagePrompt.getOptions()), imageRequest,
                        MoonshotAiImageApi.MoonshotAiImageRequest.class);
            }

            // Make the request
            ResponseEntity<MoonshotAiImageApi.MoonshotAiImageResponse> imageResponseEntity = this.moonshotAiImageApi
                    .createImage(imageRequest);

            // Convert to org.springframework.ai.model derived ImageResponse data type
            return convertResponse(imageResponseEntity, imageRequest);
        });
    }

    private ImageResponse convertResponse(ResponseEntity<MoonshotAiImageApi.MoonshotAiImageResponse> imageResponseEntity,
                                          MoonshotAiImageApi.MoonshotAiImageRequest moonshotAiImageRequest) {
        MoonshotAiImageApi.MoonshotAiImageResponse imageApiResponse = imageResponseEntity.getBody();
        if (imageApiResponse == null) {
            logger.warn("No image response returned for request: {}", moonshotAiImageRequest);
            return new ImageResponse(List.of());
        }

        List<ImageGeneration> imageGenerationList = imageApiResponse.data().stream().map(entry -> {
            return new ImageGeneration(new Image(entry.url(), entry.b64Json()),
                    new MoonshotAiImageGenerationMetadata(entry.revisedPrompt()));
        }).toList();

        ImageResponseMetadata moonshotAiImageResponseMetadata = MoonshotAiImageResponseMetadata.from(imageApiResponse);
        return new ImageResponse(imageGenerationList, moonshotAiImageResponseMetadata);
    }

    /**
     * Convert the {@link ImageOptions} into {@link MoonshotAiImageOptions}.
     * @param runtimeImageOptions the image options to use.
     * @return the converted {@link MoonshotAiImageOptions}.
     */
    private MoonshotAiImageOptions toMoonshotAiImageOptions(ImageOptions runtimeImageOptions) {
        MoonshotAiImageOptions.Builder moonshotAiImageOptionsBuilder = MoonshotAiImageOptions.builder();
        if (runtimeImageOptions != null) {
            // Handle portable image options
            if (runtimeImageOptions.getN() != null) {
                moonshotAiImageOptionsBuilder.withN(runtimeImageOptions.getN());
            }
            if (runtimeImageOptions.getModel() != null) {
                moonshotAiImageOptionsBuilder.withModel(runtimeImageOptions.getModel());
            }
            if (runtimeImageOptions.getResponseFormat() != null) {
                moonshotAiImageOptionsBuilder.withResponseFormat(runtimeImageOptions.getResponseFormat());
            }
            if (runtimeImageOptions.getWidth() != null) {
                moonshotAiImageOptionsBuilder.withWidth(runtimeImageOptions.getWidth());
            }
            if (runtimeImageOptions.getHeight() != null) {
                moonshotAiImageOptionsBuilder.withHeight(runtimeImageOptions.getHeight());
            }
            // Handle MoonshotAI specific image options
            if (runtimeImageOptions instanceof MoonshotAiImageOptions) {
                MoonshotAiImageOptions runtimeMoonshotAiImageOptions = (MoonshotAiImageOptions) runtimeImageOptions;
                if (runtimeMoonshotAiImageOptions.getQuality() != null) {
                    moonshotAiImageOptionsBuilder.withQuality(runtimeMoonshotAiImageOptions.getQuality());
                }
                if (runtimeMoonshotAiImageOptions.getStyle() != null) {
                    moonshotAiImageOptionsBuilder.withStyle(runtimeMoonshotAiImageOptions.getStyle());
                }
                if (runtimeMoonshotAiImageOptions.getUser() != null) {
                    moonshotAiImageOptionsBuilder.withUser(runtimeMoonshotAiImageOptions.getUser());
                }
            }
        }
        return moonshotAiImageOptionsBuilder.build();
    }

}
