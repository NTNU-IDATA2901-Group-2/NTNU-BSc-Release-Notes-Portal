package no.reliablesolutions.release_notes_portal.dto;

/**
 * DTO class for creating a new change note.
 */
public record CreateChangeNoteDTO(
    String title,
    String reference,
    String description,
    String developerNotes,
    String upgradeNotes,
    Long productId,
    Long scopeId,
    Long featureId,
    Long customerId,
    Boolean published,
    Boolean viewableByEveryone) {
}
