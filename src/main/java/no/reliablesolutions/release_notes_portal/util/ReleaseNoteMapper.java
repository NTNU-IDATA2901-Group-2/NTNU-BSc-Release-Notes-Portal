package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteDTO;

public class ReleaseNoteMapper {

  private ReleaseNoteMapper() {
    // Private constructor to prevent instantiation
  }

  public static ReleaseNoteDTO toDTO(ReleaseNote releaseNote, AccessScope accessScope) {
    List<ChangeNoteDTO> changeNoteDTOs = releaseNote.getChangeNotes()
        .stream()
        .map(changeNote -> ChangeNoteMapper.toDTO(changeNote, accessScope))
        .toList();

    return new ReleaseNoteDTO(
        releaseNote.getId(),
        changeNoteDTOs,
        releaseNote.getTag(),
        releaseNote.getSummary(),
        releaseNote.getPublished(),
        releaseNote.getCreatedAt());
  }

}
