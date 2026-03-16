package no.reliablesolutions.release_notes_portal.util;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiWrapper {
    private final ChatClient.Builder chatClientBuilder;
    private final String masterPrompt;

    public AiWrapper(@Value("") String masterPrompt,
            ChatClient.Builder chatClientBuilder) {
        if (masterPrompt == null || masterPrompt.isEmpty()) {
            this.masterPrompt = "";
        } else {
            this.masterPrompt = masterPrompt;
        }

        if (chatClientBuilder == null) {
            throw new IllegalArgumentException("ChatClientBuilder cannot be null");
        }
        this.chatClientBuilder = chatClientBuilder;
    }

    public String getResponse(String prompt) {
        try {
            return chatClientBuilder
            .defaultSystem(this.masterPrompt)
            .build()
            .prompt()
            .user(prompt)
            .call()
            .content();
        } catch (Exception e) {
            throw new RuntimeException("Error while calling AI service: " + e.getMessage(), e);
        }
    }       
}