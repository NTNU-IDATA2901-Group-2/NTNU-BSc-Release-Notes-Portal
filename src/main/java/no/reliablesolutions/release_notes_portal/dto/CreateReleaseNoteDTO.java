package no.reliablesolutions.release_notes_portal.dto;

import java.util.List;

/**
 * DTO class for creating/updating a release note.
 */
public record CreateReleaseNoteDTO(
    List<Long> changeNoteIds,
    String tag,
    String summary,
    Boolean published,
    ReleaseTimelineDTO releaseTimeline) {

}