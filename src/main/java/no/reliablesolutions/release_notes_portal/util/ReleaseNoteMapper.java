package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeImpactDTO;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseTimelineDTO;

/**
 * A utility class for mapping ReleaseNote entities to ReleaseNoteDTOs.
 */
public class ReleaseNoteMapper {

  private ReleaseNoteMapper() {
    // Private constructor to prevent instantiation
  }

  /**
   * Maps a ReleaseNote entity to a ReleaseNoteDTO, including or excluding certain
   * fields based on the user's access scope.
   * 
   * @param releaseNote the ReleaseNote entity to be mapped
   * @param accessScope the AccessScope of the user, which determines which fields
   *                    are included in the resulting ReleaseNoteDTO
   * @return a ReleaseNoteDTO representing the given ReleaseNote entity, with
   *         fields included or excluded based on the user's access scope
   */
  public static ReleaseNoteDTO toDTO(ReleaseNote releaseNote, AccessScope accessScope) {
    List<String> customerGroups = accessScope.getCustomerGroups();
    List<ChangeNoteDTO> changeNoteDTOs = releaseNote.getChangeNotes()
        .stream()
        .filter(changeNote -> {
          if (accessScope.isAdmin()) {
            return true;
          }

          if (!changeNote.isPublished()) {
            return false;
          }

          if (!changeNote.isViewableByEveryone() && changeNote.getCustomer() != null) {
            return customerGroups.contains(changeNote.getCustomer().getName().toUpperCase());
          }
          
          return true;
        })
        .map(changeNote -> ChangeNoteMapper.toDTO(changeNote, accessScope))
        .toList();

    List<String> knownLimitations = accessScope.isAdmin() ? releaseNote.getKnownLimitations() : List.of();
    List<ChangeImpactDTO> changeImpactDTOs = accessScope.isAdmin()
        ? releaseNote.getChangeImpacts().stream().map(ChangeImpactDTO::fromEntity).toList()
        : List.of();

    return new ReleaseNoteDTO(
        releaseNote.getId(),
        changeNoteDTOs,
        releaseNote.getTag(),
        releaseNote.getSummary(),
        releaseNote.getPublished(),
        releaseNote.getCreatedAt(),
        releaseNote.getSyncedToGit(),
        ReleaseTimelineDTO.fromEntity(releaseNote.getReleaseTimeline()),
        knownLimitations,
        changeImpactDTOs);
  }

}
