package no.reliablesolutions.release_notes_portal.dto;

/**
 * A generic DTO for paginated responses, containing the content of the current page and the total number of pages.
 */
public record PaginatedResponseDTO<T>(T content, int totalPages) {
}
