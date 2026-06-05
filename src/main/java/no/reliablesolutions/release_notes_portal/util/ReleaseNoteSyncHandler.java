package no.reliablesolutions.release_notes_portal.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import no.reliablesolutions.release_notes_portal.exception.FailedSyncReleaseNoteException;
import no.reliablesolutions.release_notes_portal.runner.ChangeNotesSyncHandler;

/**
 * Commits and pushes release notes to their associated Git repositories.
 *
 * <p>Disabled under the {@code ci} profile since it performs real filesystem and Git operations.
 */
@Component
@Profile("!ci")
public class ReleaseNoteSyncHandler {
  
  private final Logger logger = LoggerFactory.getLogger(ReleaseNoteSyncHandler.class);

  private final ChangeNotesSyncHandler syncGitChangeNotes;

  private final String releaseNoteDirectory;
  private final String githubPat;

  private final String git_author_committer_name = "Release Notes Portal";
  private final String git_author_committer_email = "release-notes-portal@example.com"; //TODO: Use actual email

  public ReleaseNoteSyncHandler(
    @Value("${RELEASE_NOTE_DIRECTORY}") String releaseNoteDirectory,
    @Value("${GITHUB_RW_PAT}") String githubPat,
    ChangeNotesSyncHandler syncGitChangeNotes
  ) {
    this.releaseNoteDirectory = releaseNoteDirectory;
    this.githubPat = githubPat;
    this.syncGitChangeNotes = syncGitChangeNotes;
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
  public boolean syncReleaseNoteToGit(ReleaseNote releaseNote, List<GitRepository> gitRepositories) {
    List<File> committedRepositoriesDirectories = new ArrayList<>();
    List<File> pushedRepositoriesDirectories = new ArrayList<>();
    File repositoryDirectory = null;
    boolean isSuccess = true;

    try {
      for (GitRepository gitRepository : gitRepositories) {
        syncGitChangeNotes.syncGitRepository(gitRepository); // ensure repository is up to date
        repositoryDirectory = prepareRepositoryDirectory(gitRepository);
        prepareReleaseNoteDirectory(repositoryDirectory);
      }
      for (GitRepository gitRepository : gitRepositories) {
        List<ChangeNote> changeNotes = releaseNote.getChangeNotes();
        List<ChangeNote> changeNotesInThisGitRepo = changeNotes.stream()
          .filter(changeNote -> changeNote.getGitRepository() != null && changeNote.getGitRepository().getId() == gitRepository.getId())
          .toList();

        List<ChangeNote> changeNotesNotInThisGitRepo = changeNotes.stream()
          .filter(changeNote -> changeNote.getGitRepository() == null || changeNote.getGitRepository().getId() != gitRepository.getId())
          .toList();

        commitLocally(repositoryDirectory, releaseNote, changeNotesInThisGitRepo, changeNotesNotInThisGitRepo, gitRepository);
        committedRepositoriesDirectories.add(repositoryDirectory);
      }
      for (File dir : committedRepositoriesDirectories) {
        pushToRemote(dir);
        pushedRepositoriesDirectories.add(dir);
      }
    } catch (Exception e) {
      logger.error("Failed to sync release note with id {} to Git: {}", releaseNote.getId(), e.getMessage());
      revert();
      isSuccess = false;
    }
    return isSuccess;

  }

  private File prepareRepositoryDirectory(GitRepository gitRepository) {
    File repositoryDirectory = new File(gitRepository.getLocalPath());
    if ((!repositoryDirectory.exists() || !repositoryDirectory.isDirectory()) && !repositoryDirectory.mkdirs()) {
      throw new IllegalStateException("Failed to create local directory for Git repository " + gitRepository.getName());
    }
    return repositoryDirectory;
  }

  private File prepareReleaseNoteDirectory(File repositoryDirectory) {
    File releaseNoteDir = new File(repositoryDirectory, releaseNoteDirectory);
    if ((!releaseNoteDir.exists() || !releaseNoteDir.isDirectory()) && !releaseNoteDir.mkdirs()) {
      throw new IllegalStateException("Failed to create release note directory in local Git repository at " + releaseNoteDir.getAbsolutePath());
    }
    return releaseNoteDir;
  }

  private void commitLocally(
    File repositoryDirectory,
    ReleaseNote releaseNote,
    List<ChangeNote> changeNotesInThisGitRepo,
    List<ChangeNote> changeNotesNotInThisGitRepo,
    GitRepository gitRepository
  ) {
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory must be prepared before committing");
    }

    String releaseNoteFileName = "release_note_" + releaseNote.getId() + ".yml";

    try (Git git = Git.open(repositoryDirectory)) {
      File releaseNoteFile = new File(repositoryDirectory, releaseNoteDirectory + File.separator + releaseNoteFileName);

      writeToFile(releaseNoteFile, releaseNote, changeNotesInThisGitRepo, changeNotesNotInThisGitRepo);

      git.add()
        .addFilepattern(releaseNoteDirectory + "/" + releaseNoteFileName)
        .call();
      git.commit()
        .setMessage("Add release note: " + releaseNote.getTag())
        .setAuthor(git_author_committer_name, git_author_committer_email)
        .setCommitter(git_author_committer_name, git_author_committer_email)
        .call();
      logger.info("Release note with id {} committed to Git repository {}", releaseNote.getId(), gitRepository.getName());
    } catch (Exception e) {
      logger.error("Failed to commit release note with id {} to Git for repository {}: {}", releaseNote.getId(), gitRepository.getName(), e.getMessage());
      throw new FailedSyncReleaseNoteException(
        "Failed to commit release note with id " + releaseNote.getId() + " to Git repository " + gitRepository.getName(), e);
    }
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

  private void pushToRemote(File releaseNoteDirectory) {
    if (releaseNoteDirectory == null) {
      throw new IllegalArgumentException("Repository directory must be prepared before pushing");
    }

    try (Git git = Git.open(releaseNoteDirectory)) {
      git.push()
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubPat, ""))
        .call();
    } catch (Exception e) {
      logger.error("Failed to push committed release note to remote Git repository: {}", e.getMessage());
      throw new FailedSyncReleaseNoteException("Failed to push committed release note to remote Git repository", e);
    }
  }

  private void revert() {
    //reset locally
    //delete pushed branches
  }
    
  /**
   *     boolean success = true;
    List<ChangeNote> changeNotes = releaseNote.getChangeNotes();

    List<GitRepository> gitRepositories = changeNotes.stream()
      .filter(changeNote -> changeNote.getGitRepository() != null)
      .map(ChangeNote::getGitRepository)
      .distinct()
      .toList();

    for (GitRepository gitRepository : gitRepositories) {
      try {
        syncGitChangeNotes.syncGitRepository(gitRepository);
      } catch (Exception e) {
        logger.error("Failed to sync Git repository {} before committing release note: {}", gitRepository.getName(), e.getMessage());
        success = false;
      }
      

      
      if (success) {
        


      }
      
    
    }
    return success;
   */
}