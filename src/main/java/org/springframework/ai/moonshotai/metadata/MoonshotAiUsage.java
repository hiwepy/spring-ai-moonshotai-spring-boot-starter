package org.springframework.ai.moonshotai.metadata;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.util.Assert;

import java.util.Optional;

public class MoonshotAiUsage implements Usage {

    public static MoonshotAiUsage from(MoonshotAiApi.Usage usage) {
        return new MoonshotAiUsage(usage);
    }

    private final MoonshotAiApi.Usage usage;

    protected MoonshotAiUsage(MoonshotAiApi.Usage usage) {
        Assert.notNull(usage, "Moonshot AI MoonshotAiApi.Usage must not be null");
        this.usage = usage;
    }

    protected MoonshotAiApi.Usage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        return Optional.ofNullable(getUsage().promptTokens()).orElse(-1).longValue();
    }

    @Override
    public Long getGenerationTokens() {
        return Optional.ofNullable(getUsage().completionTokens()).orElse(-1).longValue();
    }

    @Override
    public Long getTotalTokens() {
        return Optional.ofNullable(getUsage().totalTokens()).orElse(-1).longValue();
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }

}
