package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

@Getter
public class GitRepositoryNotFoundException extends RuntimeException {
    final Long gitRepositoryId;

    public GitRepositoryNotFoundException(Long id) {
        this.gitRepositoryId = id;
        super("Git repository not found: " + id);
    }
}
