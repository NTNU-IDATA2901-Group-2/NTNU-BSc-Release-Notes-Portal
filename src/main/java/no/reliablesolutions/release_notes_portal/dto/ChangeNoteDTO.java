package no.reliablesolutions.release_notes_portal.dto;

/**
 * A data transfer object for representing a change note.
 */
public record ChangeNoteDTO(
    long id,
    String reference,
    String description,
    String developerNotes,
    String upgradeNotes,
    ProductDTO product,
    ScopeDTO scope,
    FeatureDTO feature,
    CustomerDTO customer,
    boolean published,
    Boolean viewableByEveryone,
    Long gitRepositoryId) {
}