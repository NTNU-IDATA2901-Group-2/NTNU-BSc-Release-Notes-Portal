package no.reliablesolutions.release_notes_portal.dto;

import java.util.List;

/**
 * DTO class for a release note.
 */
public record ReleaseNoteDTO(
    long id,
    List<ChangeNoteDTO> changeNotes,
    String tag,
    String summary,
    boolean published,
    long createdAt) {
}