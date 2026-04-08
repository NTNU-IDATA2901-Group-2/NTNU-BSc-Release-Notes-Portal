package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;

/**
 * A data transfer object for representing a prompt.
 */
public record PromptDTO(
    Long id,
    String name,
    String prompt) {

  /**
   * Creates a PromptDTO from a Prompt entity.
   *
   * @param prompt the Prompt entity
   * @return the PromptDTO
   */
  public static PromptDTO fromPrompt(Prompt prompt) {
    return new PromptDTO(prompt.getId(), prompt.getName(), prompt.getPrompt());
  }
}
