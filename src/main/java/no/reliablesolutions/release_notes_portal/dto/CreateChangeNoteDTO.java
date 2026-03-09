package no.reliablesolutions.release_notes_portal.dto;

public record CreateChangeNoteDTO (
  String reference,
  String description,
  String developerNotes,
  String upgradeNotes,
  String changeSource,
  Long productId,
  Long scopeId,
  Long featureId,
  Long customerId,
  Boolean published
) {}
