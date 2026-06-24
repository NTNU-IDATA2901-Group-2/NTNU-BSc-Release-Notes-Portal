package no.reliablesolutions.release_notes_portal.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO class for a release note.
 */
public record ReleaseNoteDTO(
    long id,
    List<ChangeNoteDTO> changeNotes,
    ProductDTO product,
    String tag,
    String summary,
    boolean published,
    Instant createdAt,
    boolean syncedToGit,
    ReleaseTimelineDTO releaseTimeline,
    List<String> knownLimitations,
    List<ChangeImpactDTO> changeImpacts) {
}