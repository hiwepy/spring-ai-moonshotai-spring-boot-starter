package org.springframework.ai.moonshotai.metadata;

import org.springframework.ai.image.ImageGenerationMetadata;
import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.moonshotai.api.MoonshotAiImageApi;
import org.springframework.util.Assert;

import java.util.Objects;

public class MoonshotAiImageGenerationMetadata implements ImageGenerationMetadata {

    private String revisedPrompt;

    public MoonshotAiImageGenerationMetadata(String revisedPrompt) {
        this.revisedPrompt = revisedPrompt;
    }

    public String getRevisedPrompt() {
        return revisedPrompt;
    }

    @Override
    public String toString() {
        return "OpenAiImageGenerationMetadata{" + "revisedPrompt='" + revisedPrompt + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoonshotAiImageGenerationMetadata that)) {
            return false;
        }
        return Objects.equals(revisedPrompt, that.revisedPrompt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(revisedPrompt);
    }

}
