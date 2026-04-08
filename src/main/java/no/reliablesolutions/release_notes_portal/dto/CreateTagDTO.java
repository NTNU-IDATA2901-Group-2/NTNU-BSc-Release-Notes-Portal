package no.reliablesolutions.release_notes_portal.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO class for creating a new tag.
 */
public record CreateTagDTO(
    @NotBlank() String name) {
}
