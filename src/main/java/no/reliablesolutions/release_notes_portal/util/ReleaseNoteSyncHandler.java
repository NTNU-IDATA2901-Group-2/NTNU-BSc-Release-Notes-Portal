package no.reliablesolutions.release_notes_portal.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
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

  private final String git_author_committer_name = "Release Notes Portal";
  private final String git_author_committer_email = "release-notes-portal@example.com"; //TODO: Use actual email

  public ReleaseNoteSyncHandler(
    @Value("${RELEASE_NOTE_DIRECTORY}") String releaseNoteDirectory,
    ChangeNotesSyncHandler syncGitChangeNotes
  ) {
    this.releaseNoteDirectory = releaseNoteDirectory;
    this.syncGitChangeNotes = syncGitChangeNotes;
  }

  /**
   * Commits and pushes the given release note to each of the specified Git repositories.
   *
   * <p>For every repository the change notes are split into those originating from that
   * repository and those from others, written to a YAML file on a dedicated branch, committed,
   * and finally pushed to the remote. Failures are logged and result in a {@code false} return
   * value rather than a thrown exception.
   *
   * @param releaseNote the release note to commit
   * @param gitRepositories the target repositories to commit and push the release note into
   * @return {@code true} if the release note was committed and pushed to all repositories,
   *         {@code false} if any step failed
   */
  public boolean syncReleaseNoteToGit(ReleaseNote releaseNote, List<GitRepository> gitRepositories) {
    List<Pair<GitRepository, File>> preparedRepositoriesDirectories = new ArrayList<>();
    List<Pair<GitRepository, File>> committedRepositoriesDirectories = new ArrayList<>();
    List<Pair<GitRepository, File>> pushedRepositoriesDirectories = new ArrayList<>();

    boolean isSuccess = true;
    String branchName = getBranchNameForReleaseNote(releaseNote);

    try {
      for (GitRepository gitRepository : gitRepositories) {
        syncGitChangeNotes.syncGitRepository(gitRepository); // ensure repository is up to date
        File repositoryDirectory = prepareRepositoryDirectory(gitRepository);
        prepareReleaseNoteDirectory(repositoryDirectory);
        preparedRepositoriesDirectories.add(new Pair<>(gitRepository, repositoryDirectory));
      }
      for (Pair<GitRepository, File> repoPair : preparedRepositoriesDirectories) {
        GitRepository gitRepository = repoPair.key();
        File repositoryDirectory = repoPair.value();
        List<ChangeNote> changeNotes = releaseNote.getChangeNotes();
        List<ChangeNote> changeNotesInThisGitRepo = changeNotes.stream()
          .filter(changeNote -> changeNote.getGitRepository() != null && changeNote.getGitRepository().getId() == gitRepository.getId())
          .toList();

        List<ChangeNote> changeNotesNotInThisGitRepo = changeNotes.stream()
          .filter(changeNote -> changeNote.getGitRepository() == null || changeNote.getGitRepository().getId() != gitRepository.getId())
          .toList();
        checkoutNewBranch(repositoryDirectory, branchName);
        committedRepositoriesDirectories.add(repoPair);
        commit(repositoryDirectory, releaseNote, changeNotesInThisGitRepo, changeNotesNotInThisGitRepo, gitRepository);
      }
      for (Pair<GitRepository, File> repoPair : committedRepositoriesDirectories) {
        pushToRemote(repoPair.key(), repoPair.value(), releaseNote);
        pushedRepositoriesDirectories.add(repoPair);
      }
    } catch (Exception e) {
      logger.error("Failed to sync release note with id {} to Git. Reverting changes", releaseNote.getId(), e);
      String completeBranchName = String.format("refs/heads/%s", branchName);
      deleteLocalBranches(committedRepositoriesDirectories, completeBranchName);
      deletePushedBranches(pushedRepositoriesDirectories, completeBranchName);
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

  private void checkoutNewBranch(File repositoryDirectory, String branchName) {
    try (Git git = Git.open(repositoryDirectory)) {
      git.checkout()
        .setCreateBranch(true)
        .setName(branchName)
        .call();
    } catch (Exception e) {
      throw new FailedSyncReleaseNoteException(
        "Failed to checkout new branch " + branchName + " in Git repository at " + repositoryDirectory.getAbsolutePath(), e);
    }
  }

  private void commit(
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
      throw new FailedSyncReleaseNoteException("Failed to write release note to file " + file.getAbsolutePath(), e);
    }
  }

  private void pushToRemote(GitRepository gitRepository, File releaseNoteDirectory, ReleaseNote releaseNote) {
    if (releaseNoteDirectory == null) {
      throw new IllegalArgumentException("Repository directory must be prepared before pushing");
    }
    if (!gitRepository.isPatSet()) {
      throw new FailedSyncReleaseNoteException("No personal access token configured for Git repository " + gitRepository.getName());
    }

    try (Git git = Git.open(releaseNoteDirectory)) {
      git.push()
        .setRemote("origin")
        .setRefSpecs(new RefSpec(getBranchNameForReleaseNote(releaseNote)))
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitRepository.getPat(), ""))
        .call();
    } catch (Exception e) {
      throw new FailedSyncReleaseNoteException("Failed to push committed release note to remote Git repository", e);
    }
  }

  private void deleteLocalBranches(List<Pair<GitRepository, File>> committedRepositoriesDirectories, String completeBranchName) {
    for (Pair<GitRepository, File> repoPair : committedRepositoriesDirectories) {
      File repositoryDirectory = repoPair.value();
      try (Git git = Git.open(repositoryDirectory)) {
        String mainBranchName;
        List<Ref> branches = git.branchList().call();  // local branches
        Set<String> names = branches.stream()
            .map(Ref::getName)
            .collect(Collectors.toSet());

        if (names.contains("refs/heads/main")) {
            mainBranchName = "main";
        } else if (names.contains("refs/heads/master")) {
            mainBranchName = "master";
        } else {
            throw new IllegalStateException("Neither main nor master found");
        }
        git.checkout()
          .setName(mainBranchName)
          .call();

        git.branchDelete()
          .setBranchNames(completeBranchName)
          .setForce(true)
          .call(); //delete branch locally
        logger.info("Deleted local branch {} in Git repository at {}", completeBranchName, repositoryDirectory.getAbsolutePath());
      } catch (Exception e) {
        logger.error("Failed to delete local branch in Git repository at {}", repositoryDirectory.getAbsolutePath(), e);
      }
    }
  }

  private void deletePushedBranches(List<Pair<GitRepository, File>> pushedRepositoriesDirectories, String completeBranchName) {
    for (Pair<GitRepository, File> repoPair : pushedRepositoriesDirectories) {
      File repositoryDirectory = repoPair.value();
      try (Git git = Git.open(repositoryDirectory)) {

        RefSpec refSpec = new RefSpec()
          .setSource(null)
          .setDestination(completeBranchName);

        git.push()
          .setRefSpecs(refSpec)
          .setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoPair.key().getPat(), ""))
          .setRemote("origin")
          .call(); //delete branch remotely

        logger.info("Deleted pushed branch {} in Git repository at {}", completeBranchName, repositoryDirectory.getAbsolutePath());
      } catch (Exception e) {
        logger.error("Failed to delete pushed branch in Git repository at {}", repositoryDirectory.getAbsolutePath(), e);
      }
    }
  }
  
  private String getBranchNameForReleaseNote(ReleaseNote releaseNote) {
    return "release-note-" + releaseNote.getId();
  }

  /**
   * Simple immutable key/value pair used to keep a Git repository associated with its
   * prepared local directory while syncing.
   */
  private record Pair<K, V>(K key, V value) {}
}
