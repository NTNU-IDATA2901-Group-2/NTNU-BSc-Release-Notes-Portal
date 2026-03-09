package no.reliablesolutions.release_notes_portal.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagDTO(
  @NotBlank()
  String name
) {}
