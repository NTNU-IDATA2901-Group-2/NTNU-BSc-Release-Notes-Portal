package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;

public record PromptDTO(
    Long id,
    String name,
    String prompt
) {
    public static PromptDTO fromPrompt(Prompt prompt) {
        return new PromptDTO(prompt.getId(), prompt.getName(), prompt.getPrompt());
    }
}
