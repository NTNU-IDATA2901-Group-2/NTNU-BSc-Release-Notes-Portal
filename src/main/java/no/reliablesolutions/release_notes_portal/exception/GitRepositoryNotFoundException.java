package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a Git repository with the specified ID is not found.
 */
@Getter
public class GitRepositoryNotFoundException extends RuntimeException {
    final Long gitRepositoryId;

    /**
     * Constructs a new GitRepositoryNotFoundException with the specified Git repository ID.
     *
     * @param id the ID of the Git repository that was not found
     */
    public GitRepositoryNotFoundException(Long id) {
        this.gitRepositoryId = id;
        super("Git repository not found: " + id);
    }
}
