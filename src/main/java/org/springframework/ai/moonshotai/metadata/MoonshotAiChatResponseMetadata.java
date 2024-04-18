package org.springframework.ai.moonshotai.metadata;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;

import java.util.Optional;

public class MoonshotAiChatResponseMetadata implements ChatResponseMetadata {

    private final MoonshotAiApi.Usage usage;

    public MoonshotAiChatResponseMetadata(MoonshotAiApi.Usage usage) {
        this.usage = usage;
    }

    @Override
    public Usage getUsage() {
        return new Usage() {

            @Override
            public Long getPromptTokens() {
                return Optional.ofNullable(usage.promptTokens()).orElse(-1).longValue();
            }

            @Override
            public Long getGenerationTokens() {
                return Optional.ofNullable(usage.completionTokens()).orElse(-1).longValue();
            }

            @Override
            public Long getTotalTokens() {
                return Optional.ofNullable(usage.totalTokens()).orElse(-1).longValue();
            }
        };
    }

}
