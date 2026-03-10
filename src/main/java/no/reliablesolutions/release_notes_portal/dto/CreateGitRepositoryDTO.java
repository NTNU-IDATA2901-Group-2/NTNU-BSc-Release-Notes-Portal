package no.reliablesolutions.release_notes_portal.dto;

/**
 * DTO class for creating a new Git repository.
 */
public record CreateGitRepositoryDTO(
    String name,
    String url
) {}

