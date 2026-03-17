package no.reliablesolutions.release_notes_portal.dto;

public record ChangeNoteDTO(
    long id,
    String reference,
    String description,
    String developerNotes,
    String upgradeNotes,
    String changeSource,
    ProductDTO product,
    ScopeDTO scope,
    FeatureDTO feature,
    CustomerDTO customer,
    boolean published) {
}