package org.springframework.ai.moonshotai.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.ai.moonshotai.api.MoonshotAiEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MoonshotAiEmbeddingProperties.CONFIG_PREFIX)
public class MoonshotAiEmbeddingProperties extends MoonshotAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.moonshotai.embedding";

    public static final String DEFAULT_EMBEDDING_MODEL = MoonshotAiApi.EmbeddingModel.EMBED.getValue();

    /**
     * Enable Moonshot AI embedding client.
     */
    private boolean enabled = true;

    public MetadataMode metadataMode = MetadataMode.EMBED;

    /**
     * Client lever Moonshot AI options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private MoonshotAiEmbeddingOptions options = MoonshotAiEmbeddingOptions.builder()
            .withModel(DEFAULT_EMBEDDING_MODEL)
            .build();

    public MoonshotAiEmbeddingOptions getOptions() {
        return this.options;
    }

    public void setOptions(MoonshotAiEmbeddingOptions options) {
        this.options = options;
    }

    public MetadataMode getMetadataMode() {
        return this.metadataMode;
    }

    public void setMetadataMode(MetadataMode metadataMode) {
        this.metadataMode = metadataMode;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
