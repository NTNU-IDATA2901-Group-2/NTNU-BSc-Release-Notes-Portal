package no.reliablesolutions.release_notes_portal.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.exception.DiffStringGenerationException;
import no.reliablesolutions.release_notes_portal.runner.SyncGitChangeNotes;

@Service
@Profile("!ci")
class DiffService {

  private final Logger logger = LoggerFactory.getLogger(DiffService.class);
  private final String changeNoteDirectory;
  /**
   * Constructor for DiffService.
   * @param repositoryDirectoriesPath the base path where local git repositories are stored, injected from application properties
   */
  public DiffService(
    @Value("${CHANGE_NOTE_DIRECTORY}") String changeNoteDirectory
  ) {
    this.changeNoteDirectory = changeNoteDirectory;
  }

  /**
   * Generates a diff string between two commits for a given git repository.
   * @param commitHash the hash of the new commit
   * @param previousCommitHash the hash of the previous commit
   * @param gitRepository the git repository for which the diff is to be generated
   * @return a string representation of the diff between the two commits
   * @throws IllegalArgumentException if any of the parameters are null or if the repository directory does not exist
   * @throws RuntimeException if there is an error while generating the diff string
   */
  public String getDiffString(String commitHash, String previousCommitHash, GitRepository gitRepository) throws DiffStringGenerationException {
    if (commitHash == null || previousCommitHash == null) {
      throw new IllegalArgumentException("Commit hashes cannot be null");
    }

    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }

    File repositoryDirectory = new File(gitRepository.getLocalPath(SyncGitChangeNotes.REPOSITORY_DIRECTORIES_PATH));
    if (!repositoryDirectory.exists()) {
      throw new IllegalArgumentException("Repository directory does not exist: " + repositoryDirectory.getAbsolutePath());
    }

    StringBuilder diffStringBuilder = new StringBuilder();

    try (Git git = Git.open(repositoryDirectory);
    Repository repository = git.getRepository();
    OutputStream outputStream = new ByteArrayOutputStream();
    DiffFormatter diffFormatter = new DiffFormatter(outputStream);) {
      
      diffFormatter.setRepository(repository);

      RevCommit newCommit = repository.parseCommit(repository.resolve(commitHash));
      RevCommit oldCommit = repository.parseCommit(repository.resolve(previousCommitHash));

      List<DiffEntry> diffs = diffFormatter.scan(oldCommit.getTree(), newCommit.getTree());
      for (DiffEntry diff : diffs) {
        if (!diff.getNewPath().startsWith(changeNoteDirectory)) {
          diffFormatter.format(diff);
        }
      }
      diffStringBuilder.append(outputStream.toString());
    } catch (Exception e) {
      logger.error("Error generating diff string for commit {} and previous commit {} in repository {}: {}", commitHash, previousCommitHash, gitRepository.getName(), e.getMessage());
      throw new DiffStringGenerationException("Error generating diff string: " + e.getMessage());
    }

    logger.info("Generated diff string for commit {} and previous commit {} in repository {}", commitHash, previousCommitHash, gitRepository.getName());
    return diffStringBuilder.toString();
  }
}