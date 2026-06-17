package no.reliablesolutions.release_notes_portal.dto;

import java.util.List;

import jakarta.validation.Valid;

/**
 * DTO class for creating/updating a release note.
 */
public record CreateReleaseNoteDTO(
    List<Long> changeNoteIds,
    String tag,
    String summary,
    Boolean published,
    ReleaseTimelineDTO releaseTimeline,
    List<String> knownLimitations,
    @Valid List<CreateChangeImpactDTO> changeImpacts) {

}