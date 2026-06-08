package no.reliablesolutions.release_notes_portal.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.domain.repository.ChangeNoteRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.ReleaseNoteRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateGitRepositoryDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedSyncGitChangeNotesException;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.GitRepositoryNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ReleaseNoteAlreadySyncedException;
import no.reliablesolutions.release_notes_portal.exception.ReleaseNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.runner.ChangeNotesSyncHandler;
import no.reliablesolutions.release_notes_portal.util.ReleaseNoteSyncHandler;

/**
 * Service class for managing Git repositories, including creating, deleting, updating, and synchronizing repositories.
 */
@Service
@AllArgsConstructor
public class GitRepositoryService {
    private final GitRepositoryRepository gitRepositoryRepository;
    private final ObjectProvider<ChangeNotesSyncHandler> syncGitChangeNotesProvider;
    private final ObjectProvider<ReleaseNoteSyncHandler> releaseNoteSyncHandlerProvider;
    private final ChangeNoteRepository changeNoteRepository;
    private final ReleaseNoteRepository releaseNoteRepository;

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
        changeNoteRepository.clearGitRepositoryReferencesById(id);
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
            ChangeNotesSyncHandler syncGitChangeNotes = syncGitChangeNotesProvider.getIfAvailable();
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

            ChangeNotesSyncHandler syncGitChangeNotes = syncGitChangeNotesProvider.getIfAvailable();
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


  /**
   * Commits a release note to every Git repository associated with its change notes,
   * plus any additional repositories explicitly requested.
   *
   * @param id the id of the release note to commit
   * @param additionalGitRepositoryIds ids of extra repositories to commit to
   * @throws ReleaseNoteNotFoundException if the release note does not exist or is archived
   * @throws GitRepositoryNotFoundException if an additional repository id does not exist
   */
  public boolean commitReleaseNoteToGit(long id, List<Long> additionalGitRepositoryIds) {
    ReleaseNoteSyncHandler releaseNoteSyncHandler = releaseNoteSyncHandlerProvider.getIfAvailable();
    if (releaseNoteSyncHandler == null) {
      throw new IllegalStateException("ReleaseNoteSyncHandler is not available. Cannot sync release note to Git.");
    }

    Optional<ReleaseNote> releaseNoteOptional = releaseNoteRepository.findById(id);
    if (releaseNoteOptional.isEmpty() || Boolean.TRUE.equals(releaseNoteOptional.get().getArchived())) {
      throw new ReleaseNoteNotFoundException(id);
    }
    ReleaseNote releaseNote = releaseNoteOptional.get();
    if (releaseNote.getChangeNotes().isEmpty()) {
        throw new IllegalArgumentException("Release note with id " + id + " has no change notes to commit");
    }
    if (Boolean.TRUE.equals(releaseNote.getSyncedToGit())) {
        throw new ReleaseNoteAlreadySyncedException(id);
    }

    Set<GitRepository> gitRepositories = new HashSet<>();
    additionalGitRepositoryIds.forEach(repoId -> {
      GitRepository gitRepository = gitRepositoryRepository.findById(repoId)
          .orElseThrow(() -> new GitRepositoryNotFoundException(repoId));
      gitRepositories.add(gitRepository);
    });
    releaseNote.getChangeNotes().forEach(changeNote -> {
      GitRepository gitRepository = gitRepositoryRepository.findByChangeNoteId(changeNote.getId());
      if (gitRepository != null) {
        gitRepositories.add(gitRepository);
      }
    });
    boolean success = releaseNoteSyncHandler.syncReleaseNoteToGit(releaseNote, gitRepositories.stream().toList());
    if (success) {
      releaseNote.setSyncedToGit(true);
      releaseNoteRepository.save(releaseNote);
    }
    return success;
  }
}
