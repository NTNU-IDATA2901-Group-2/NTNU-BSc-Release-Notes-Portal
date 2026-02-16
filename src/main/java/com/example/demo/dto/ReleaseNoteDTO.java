package com.example.demo.dto;

import java.util.List;

import com.example.demo.domain.entity.ReleaseNote;

/**
 * DTO class for a release note.
 */
public record ReleaseNoteDTO(
    long id,
    List<ChangeNoteDTO> changeNotes,
    String tag,
    String summary,
    boolean published,
    long createdAt,
    boolean archived) {

    /**
     * Converts a ReleaseNote entity to a ReleaseNoteDTO.
     *
     * @param releaseNote the ReleaseNote entity to be converted
     * @return the corresponding ReleaseNoteDTO
     */
    public static ReleaseNoteDTO fromReleaseNote(ReleaseNote releaseNote) {
        List<ChangeNoteDTO> changeNoteDTOs = releaseNote.getChangeNotes()
            .stream()
            .map(changeNote -> ChangeNoteDTO.fromChangeNote(changeNote))
            .toList();
        ReleaseNoteDTO releaseNoteDTO = new ReleaseNoteDTO(
            releaseNote.getId(),
            changeNoteDTOs,
            releaseNote.getTag(),
            releaseNote.getSummary(),
            releaseNote.getPublished(),
            releaseNote.getCreatedAt(),
            releaseNote.getArchived());
        return releaseNoteDTO;
    }
}