package no.reliablesolutions.release_notes_portal.dto;

public record PromptDTO(
    Long id,
    String name,
    String prompt
) {
    public static PromptDTO fromPrompt(no.reliablesolutions.release_notes_portal.domain.entity.Prompt prompt) {
        return new PromptDTO(prompt.getId(), prompt.getName(), prompt.getPrompt());
    }
}
