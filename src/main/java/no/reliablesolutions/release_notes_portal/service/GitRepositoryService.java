package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateGitRepositoryDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedSyncGitChangeNotesException;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.GitRepositoryNotFoundException;
import no.reliablesolutions.release_notes_portal.runner.SyncGitChangeNotes;

/**
 * Service class for managing Git repositories, including creating, deleting, updating, and synchronizing repositories.
 */
@Service
@AllArgsConstructor
public class GitRepositoryService {
    private final GitRepositoryRepository gitRepositoryRepository;
    private final ObjectProvider<SyncGitChangeNotes> syncGitChangeNotesProvider;

    /**
     * Creates a new Git repository based on the provided CreateGitRepositoryDTO.
     *
     * @param dto the DTO containing the name and URL of the Git repository to create
     * @return the ID of the newly created Git repository
     * @throws FailedToSaveEntityException if saving the Git repository to the database fails
     */
    public long createGitRepository(CreateGitRepositoryDTO dto) {
        var gitRepository = new GitRepository();
        gitRepository.setName(dto.name());
        gitRepository.setUrl(dto.url());
        try {
            return gitRepositoryRepository.save(gitRepository).getId();
        } catch (Exception _) {
            throw new FailedToSaveEntityException("Failed to create Git repository");
        }
    }

    /**
     * Deletes a Git repository by its ID.
     *
     * @param id the ID of the Git repository to delete
     * @throws GitRepositoryNotFoundException if the Git repository with the specified ID is not found
     */
    public void deleteGitRepository(long id) {
        gitRepositoryRepository.findById(id).orElseThrow(() -> new GitRepositoryNotFoundException(id));
        gitRepositoryRepository.clearGitRepositoryReferencesById(id);
        gitRepositoryRepository.deleteById(id);
    }

    /**
     * Retrieves all Git repositories.
     *
     * @return a list of all Git repositories
     */
    public List<GitRepository> getAllGitRepositories() {
        return gitRepositoryRepository.findAll();
    }

    /**
     * Updates an existing Git repository.
     *
     * @param gitRepository the Git repository to update
     * @throws FailedToSaveEntityException if saving the Git repository to the database fails
     */
    public void updateGitRepository(GitRepository gitRepository) {
        try {
            gitRepositoryRepository.save(gitRepository);
        } catch (Exception _) {
            throw new FailedToSaveEntityException("Failed to update Git repository with id " + gitRepository.getId());
        }
    }

    /**
     * Synchronizes Git repositories by running SyncGitChangeNotes.
     * 
     * @throws FailedSyncGitChangeNotesException if syncing Git change notes fails
     */
    public void syncGitRepositories() {
        try {
            SyncGitChangeNotes syncGitChangeNotes = syncGitChangeNotesProvider.getIfAvailable();
            if (syncGitChangeNotes == null) {
                throw new IllegalStateException("SyncGitChangeNotes is not available. Cannot sync Git repositories.");
            }
            syncGitChangeNotes.run();
        } catch (Exception e) {
            throw new FailedSyncGitChangeNotesException(e.getMessage());
        }
    }

    /**
     * Synchronizes a specific Git repository by ID by running SyncGitChangeNotes for that repository.
     *
     * @param id the ID of the Git repository to synchronize
     * @throws FailedSyncGitChangeNotesException if syncing Git change notes fails
     * @throws GitRepositoryNotFoundException if the Git repository with the specified ID is not found
     */
    public void syncGitRepository(long id) {
        try {
            GitRepository gitRepository = gitRepositoryRepository.findById(id).orElseThrow(() -> new GitRepositoryNotFoundException(id));

            SyncGitChangeNotes syncGitChangeNotes = syncGitChangeNotesProvider.getIfAvailable();
            if (syncGitChangeNotes == null) {
                throw new IllegalStateException("SyncGitChangeNotes is not available. Cannot sync Git repository.");
            }
            syncGitChangeNotes.syncGitRepository(gitRepository);
        } catch (GitRepositoryNotFoundException e) {
            throw e; // rethrow to handle in global exception handler
        } catch (Exception e) {
            throw new FailedSyncGitChangeNotesException(e.getMessage());
        }
    }

    /**
     * Retrieves the Git repository associated with a specific change note ID.
     *
     * @param changeNoteId the ID of the change note for which to retrieve the associated Git repository
     * @return the Git repository associated with the specified change note ID, or null if no repository is associated
     */
    public GitRepository getGitRepositoryForChangeNote(long changeNoteId) {
      return gitRepositoryRepository.findByChangeNoteId(changeNoteId);
    }
}
