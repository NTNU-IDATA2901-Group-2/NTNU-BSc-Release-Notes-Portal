package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.CustomerDTO;
import no.reliablesolutions.release_notes_portal.dto.FeatureDTO;
import no.reliablesolutions.release_notes_portal.dto.ProductDTO;
import no.reliablesolutions.release_notes_portal.dto.ScopeDTO;

/**
 * A utility class for mapping ChangeNote entities to ChangeNoteDTOs, taking
 * into account the access scope of the user.
 */
public class ChangeNoteMapper {

  private ChangeNoteMapper() {
    // Private constructor to prevent instantiation
  }

  /**
   * Maps a ChangeNote entity to a ChangeNoteDTO, including or excluding certain
   * fields based on the user's access scope.
   *
   * @param changeNote  the ChangeNote entity to be mapped
   * @param accessScope the AccessScope of the user, which determines which fields
   *                    are included in the resulting ChangeNoteDTO
   * @return a ChangeNoteDTO representing the given ChangeNote entity, with fields
   *         included or excluded based on the user's access scope
   */
  public static ChangeNoteDTO toDTO(ChangeNote changeNote, AccessScope accessScope) {
    List<Long> relatedReleaseNoteIds = changeNote.getReleaseNotes().stream()
        .filter(rn -> !Boolean.TRUE.equals(rn.getArchived()))
        .map(rn -> rn.getId())
        .toList();

    if (!accessScope.isAdmin()) {
      return new ChangeNoteDTO(
          changeNote.getId(),
          changeNote.getTitle(),
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
          changeNote.getGitRepository() != null ? changeNote.getGitRepository().getId() : null,
          changeNote.getGitCommitHash(),
          relatedReleaseNoteIds);
    } else {
      return new ChangeNoteDTO(
          changeNote.getId(),
          changeNote.getTitle(),
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
          changeNote.getGitRepository() != null ? changeNote.getGitRepository().getId() : null,
          changeNote.getGitCommitHash(),
          relatedReleaseNoteIds);
    }
  }
}
