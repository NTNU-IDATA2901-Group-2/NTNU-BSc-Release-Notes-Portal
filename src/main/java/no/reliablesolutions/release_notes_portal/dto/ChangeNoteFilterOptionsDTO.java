package no.reliablesolutions.release_notes_portal.dto;

import java.util.List;

/**
 * A data transfer object for representing filter options for change notes.
 */
public record ChangeNoteFilterOptionsDTO(
    String query,
    Boolean published,
    Boolean hasReleaseNote,
    List<Long> filteredIds,
    List<Long> customerIds,
    List<Long> featureIds,
    List<Long> scopeIds,
    List<Long> productIds) {
}
