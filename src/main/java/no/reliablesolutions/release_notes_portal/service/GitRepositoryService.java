package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateGitRepositoryDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.GitRepositoryNotFoundException;

@Service
@AllArgsConstructor
public class GitRepositoryService {
    private final GitRepositoryRepository gitRepositoryRepository;

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
}
