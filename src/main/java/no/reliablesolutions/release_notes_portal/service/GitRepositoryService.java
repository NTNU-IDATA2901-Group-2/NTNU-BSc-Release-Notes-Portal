package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateGitRepositoryDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedSyncGitChangeNotesException;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.GitRepositoryNotFoundException;
import no.reliablesolutions.release_notes_portal.runner.SyncGitChangeNotes;

@Service
@AllArgsConstructor
public class GitRepositoryService {
    private final GitRepositoryRepository gitRepositoryRepository;
    private final SyncGitChangeNotes syncGitChangeNotes;

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

    public void deleteGitRepository(long id) {
        gitRepositoryRepository.findById(id).orElseThrow(() -> new GitRepositoryNotFoundException(id));
        gitRepositoryRepository.deleteById(id);
    }

    public List<GitRepository> getAllGitRepositories() {
        return gitRepositoryRepository.findAll();
    }

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
            syncGitChangeNotes.run();
        } catch (Exception e) {
            throw new FailedSyncGitChangeNotesException(e.getMessage());
        }
    }

    /**
     * Synchronizes a specific Git repository by ID by running SyncGitChangeNotes for that repository.
     * @param id the ID of the Git repository to synchronize
     * @throws FailedSyncGitChangeNotesException if syncing Git change notes fails
     * @throws GitRepositoryNotFoundException if the Git repository with the specified ID is not found
     */
    public void syncGitRepository(long id) {
        try {
            GitRepository gitRepository = gitRepositoryRepository.findById(id).orElseThrow(() -> new GitRepositoryNotFoundException(id));
            syncGitChangeNotes.syncGitRepository(gitRepository);
        } catch (GitRepositoryNotFoundException e) {
            throw e; // rethrow to handle in global exception handler
        } catch (Exception e) {
            throw new FailedSyncGitChangeNotesException(e.getMessage());
        }
    }

    public GitRepository getGitRepositoryForChangeNote(long changeNoteId) {
      return gitRepositoryRepository.findByChangeNoteId(changeNoteId);
    }
}
