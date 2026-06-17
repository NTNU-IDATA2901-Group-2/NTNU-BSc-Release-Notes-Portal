package no.reliablesolutions.release_notes_portal.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * A data transfer object for representing filter options for release notes.
 */
public record ReleaseNoteFilterOptionsDTO(
    String query,
    Boolean published,
    Boolean includeUnassignedProduct,
    List<Long> productIds,
    LocalDate fromDate,
    LocalDate toDate) {
}
