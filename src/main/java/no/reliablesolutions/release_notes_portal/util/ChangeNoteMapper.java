package no.reliablesolutions.release_notes_portal.util;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.CustomerDTO;
import no.reliablesolutions.release_notes_portal.dto.FeatureDTO;
import no.reliablesolutions.release_notes_portal.dto.ProductDTO;
import no.reliablesolutions.release_notes_portal.dto.ScopeDTO;

public class ChangeNoteMapper {

  private ChangeNoteMapper() {
    // Private constructor to prevent instantiation
  }

  public static ChangeNoteDTO toDTO(ChangeNote changeNote, AccessScope accessScope) {
    if (!accessScope.isAdmin()) {
      return new ChangeNoteDTO(
        changeNote.getId(),
        changeNote.getReference(),
        changeNote.getDescription(),
        null,
        null,
        changeNote.getProduct() != null ? ProductDTO.fromProduct(changeNote.getProduct()) : null,
        changeNote.getScope() != null ? ScopeDTO.fromScope(changeNote.getScope()) : null,
        changeNote.getFeature() != null ? FeatureDTO.fromFeature(changeNote.getFeature()) : null,
        changeNote.getCustomer() != null ? CustomerDTO.fromCustomer(changeNote.getCustomer()) : null,
        changeNote.isPublished(),
        null,
        changeNote.getGitRepository() != null ? changeNote.getGitRepository().getId() : null
      );
    } else {
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
        changeNote.isPublished(),
        changeNote.isViewableByEveryone(),
        changeNote.getGitRepository() != null ? changeNote.getGitRepository().getId() : null
      );
    }
  }
}
