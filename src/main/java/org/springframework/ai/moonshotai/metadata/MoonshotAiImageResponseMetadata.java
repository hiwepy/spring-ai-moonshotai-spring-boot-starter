package org.springframework.ai.moonshotai.metadata;

import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.moonshotai.api.MoonshotAiImageApi;
import org.springframework.util.Assert;

import java.util.Objects;

public class MoonshotAiImageResponseMetadata implements ImageResponseMetadata {

    private final Long created;

    public static MoonshotAiImageResponseMetadata from(MoonshotAiImageApi.MoonshotAiImageResponse moonshotAiImageResponse) {
        Assert.notNull(moonshotAiImageResponse, "MoonshotAiImageResponse must not be null");
        return new MoonshotAiImageResponseMetadata(moonshotAiImageResponse.created());
    }

    protected MoonshotAiImageResponseMetadata(Long created) {
        this.created = created;
    }

    @Override
    public Long created() {
        return this.created;
    }

    @Override
    public String toString() {
        return "OpenAiImageResponseMetadata{" + "created=" + created + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MoonshotAiImageResponseMetadata that))
            return false;
        return Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(created);
    }

}
