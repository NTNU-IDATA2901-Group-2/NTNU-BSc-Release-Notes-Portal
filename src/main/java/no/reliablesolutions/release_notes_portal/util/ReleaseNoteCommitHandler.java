package no.reliablesolutions.release_notes_portal.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;

/**
 * Commits and pushes release notes to their associated Git repositories.
 *
 * <p>Disabled under the {@code ci} profile since it performs real filesystem and Git operations.
 */
@Component
@Profile("!ci")
public class ReleaseNoteCommitHandler {
  
  private final Logger logger = LoggerFactory.getLogger(ReleaseNoteCommitHandler.class);

  private final String releaseNoteDirectory;
  private final String githubPat;

  public ReleaseNoteCommitHandler(
    @Value("${RELEASE_NOTE_DIRECTORY}") String releaseNoteDirectory,
    @Value("${GITHUB_RW_PAT}") String githubPat
  ) {
    this.releaseNoteDirectory = releaseNoteDirectory;
    this.githubPat = githubPat;
  }

  /**
   * Commits and pushes the given release note to the specified Git repository.
   *
   * <p>Does nothing if no change notes belong to this repository. Failures are logged, not thrown.
   *
   * @param gitRepository the target repository to commit the release note into
   * @param releaseNote the release note to commit
   * @param changeNotes all change notes associated with the release note (across all repositories)
   */
  public boolean commitReleaseNoteToGit(ReleaseNote releaseNote) {
    boolean success = true;
    List<ChangeNote> changeNotes = releaseNote.getChangeNotes();

    List<GitRepository> gitRepositories = changeNotes.stream()
      .filter(changeNote -> changeNote.getGitRepository() != null)
      .map(ChangeNote::getGitRepository)
      .distinct()
      .toList();

    for (GitRepository gitRepository : gitRepositories) {
      File repositoryDirectory = new File(gitRepository.getLocalPath());
      if ((!repositoryDirectory.exists() || !repositoryDirectory.isDirectory()) && !repositoryDirectory.mkdirs()) {
          logger.error("Failed to create local repository directory for Git repository {}: {}", gitRepository.getName(), repositoryDirectory.getAbsolutePath());
          success = false;
      }

      File releaseNoteDir = new File(repositoryDirectory, releaseNoteDirectory);
      if ((!releaseNoteDir.exists() || !releaseNoteDir.isDirectory()) && !releaseNoteDir.mkdirs()) {
          logger.error("Failed to create release note directory for Git repository {}", gitRepository.getName());
          success = false;
      }
      
      if (success) {
        List<ChangeNote> changeNotesInThisGitRepo = changeNotes.stream()
          .filter(changeNote -> changeNote.getGitRepository() != null && changeNote.getGitRepository().getId() == gitRepository.getId())
          .toList();

        List<ChangeNote> changeNotesNotInThisGitRepo = changeNotes.stream()
          .filter(changeNote -> changeNote.getGitRepository() == null || changeNote.getGitRepository().getId() != gitRepository.getId())
          .toList();

        try (Git git = Git.open(repositoryDirectory)) {
          String releaseNoteFileName = "release_note_" + releaseNote.getId() + ".yml";
          File releaseNoteFile = new File(repositoryDirectory, releaseNoteDirectory + File.separator + releaseNoteFileName);

          writeToFile(releaseNoteFile, releaseNote, changeNotesInThisGitRepo, changeNotesNotInThisGitRepo);

          git.add()
            .addFilepattern(releaseNoteDirectory + "/" + releaseNoteFileName)
            .call();
          git.commit()
            .setMessage("Add release note: " + releaseNote.getTag())
            .setAuthor("Release Notes Portal", "release-notes-portal@example.com") //TODO: Use actual email
            .setCommitter("Release Notes Portal", "release-notes-portal@example.com") //TODO: Use actual email
            .call();
          git.push()
              .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubPat, ""))
              .call();
          logger.info("Release note with id {} committed to Git repository {}", releaseNote.getId(), gitRepository.getName());
        } catch (Exception e) {
          logger.error("Failed to commit release note with id {} to Git for repository {}: {}", releaseNote.getId(), gitRepository.getName(), e.getMessage());
          success = false;
        }
      }
      
    
    }
    return success;
  }

  /**
   * Serializes the release note and its change-note references to a YAML file.
   *
   * @param file the target YAML file to write
   * @param releaseNote the release note to serialize
   * @param changeNotesInThisGitRepo change notes originating from the target repository
   * @param changeNotesNotInThisGitRepo change notes originating from other repositories
   */
  private void writeToFile(File file, ReleaseNote releaseNote, List<ChangeNote> changeNotesInThisGitRepo, List<ChangeNote> changeNotesNotInThisGitRepo) {
    Map<String, Object> yamlContent = new LinkedHashMap<>();
    yamlContent.put("title", releaseNote.getTag());
    yamlContent.put("change-notes-this-repository", changeNotesInThisGitRepo.stream().map(ChangeNote::getReference).toList());
    yamlContent.put("change-notes-other-repositories", changeNotesNotInThisGitRepo.stream().map(ChangeNote::getReference).toList());

    try (PrintWriter writer = new PrintWriter(file)) {
      Yaml yaml = new Yaml();
      yaml.dump(yamlContent, writer);
    } catch (Exception e) {
      logger.error("Failed to write release note to file {}: {}", file.getAbsolutePath(), e.getMessage());
    }
  }
    
}