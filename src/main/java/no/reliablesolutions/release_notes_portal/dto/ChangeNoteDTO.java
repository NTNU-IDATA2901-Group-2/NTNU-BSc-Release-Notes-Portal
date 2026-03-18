package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;

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
    boolean published) {
  public static ChangeNoteDTO fromChangeNote(ChangeNote changeNote) {
    return new ChangeNoteDTO(
        changeNote.getId(),
        changeNote.getReference(),
        changeNote.getDescription(),
        changeNote.getDeveloperNotes(),
        changeNote.getUpgradeNotes(),
        changeNote.getProduct() != null ? ProductDTO.fromProduct(changeNote.getProduct()) : null,
        changeNote.getScope() != null ? ScopeDTO.fromScope(changeNote.getScope()) : null,
        changeNote.getFeature() != null ? FeatureDTO.fromFeature(changeNote.getFeature()) : null,
        changeNote.getCustomer() != null ? CustomerDTO.fromCustomer(changeNote.getCustomer()) : null,
        changeNote.isPublished());
  }
}