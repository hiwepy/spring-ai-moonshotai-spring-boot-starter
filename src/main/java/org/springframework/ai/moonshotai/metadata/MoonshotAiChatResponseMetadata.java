package org.springframework.ai.moonshotai.metadata;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.moonshotai.api.MoonshotAiApi;
import org.springframework.util.Assert;

public class MoonshotAiChatResponseMetadata implements ChatResponseMetadata {

    public static MoonshotAiChatResponseMetadata from(MoonshotAiApi.ChatCompletion chatCompletion) {
        Assert.notNull(chatCompletion, "MoonshotAI ChatCompletion must not be null");
        MoonshotAiUsage usage = MoonshotAiUsage.from(chatCompletion.usage());
        MoonshotAiChatResponseMetadata chatResponseMetadata = new MoonshotAiChatResponseMetadata(chatCompletion.id(), usage);
        return chatResponseMetadata;
    }

    private final String id;
    private final Usage usage;

    public MoonshotAiChatResponseMetadata(String id, MoonshotAiUsage usage) {
        this.id = id;
        this.usage = usage;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Usage getUsage() {
        Usage usage = this.usage;
        return usage != null ? usage : new EmptyUsage();
    }

}
