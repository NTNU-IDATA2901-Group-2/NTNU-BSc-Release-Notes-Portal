package no.reliablesolutions.release_notes_portal.runner;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.exception.FailedSyncGitChangeNotesException;
import no.reliablesolutions.release_notes_portal.exception.InvalidChangeNoteYamlException;
import no.reliablesolutions.release_notes_portal.service.ChangeNoteService;
import no.reliablesolutions.release_notes_portal.util.ChangeNoteFileHandler;

/**
 * This class is responsible for synchronizing change notes from Git repositories. It will clone or update the repositories, check for new commits, and create change notes from any new change note files found in the commits.
 */
@Component
@Profile("!ci")
public class ChangeNotesSyncHandler implements CommandLineRunner {
  
  private final Logger logger = LoggerFactory.getLogger(ChangeNotesSyncHandler.class);
  private final GitRepositoryRepository gitRepositoryRepository;
  private final ChangeNoteService changeNoteService;
  private final ChangeNoteFileHandler changeNoteFileHandler;
  
  // local directory for git repositories, relative to the application working directory
  public static final String REPOSITORY_DIRECTORIES_PATH = "git_repositories";

  /**
   * Constructor for SyncGitChangeNotes.
   *
   * @param gitRepositoryRepository the repository for accessing GitRepository entities
   * @param changeNoteService the service for managing change notes
   * @param changeNoteFileHandler the utility for handling change note files
   */
  public ChangeNotesSyncHandler(
    GitRepositoryRepository gitRepositoryRepository,
    ChangeNoteService changeNoteService,
    ChangeNoteFileHandler changeNoteFileHandler
  ) {
    this.gitRepositoryRepository = gitRepositoryRepository;
    this.changeNoteService = changeNoteService;
    this.changeNoteFileHandler = changeNoteFileHandler;
  }

  /**
   * Runs the synchronization process for Git change notes on all Git repositories at application startup. Failures are logged without aborting startup.
   */
  @Override
  public void run(String... args) throws Exception {
    try {
      this.syncAllGitRepositories();
    } catch (FailedSyncGitChangeNotesException e) {
      logger.error("Git repository synchronization at startup finished with failures", e);
    }
  }

  /**
   * Synchronizes change notes from all Git repositories. Every repository is attempted, even if some fail.
   *
   * @throws FailedSyncGitChangeNotesException if one or more repositories failed to synchronize
   */
  public void syncAllGitRepositories() {
    List<GitRepository> gitRepositories = gitRepositoryRepository.findAll();
    logger.info("Found {} git repositories", gitRepositories.size());
    List<String> failedRepositoryNames = new ArrayList<>();
    for (GitRepository gitRepository : gitRepositories) {
      try {
        this.syncGitRepository(gitRepository);
      } catch (Exception e) {
        logger.error("Failed to synchronize Git repository with id {}", gitRepository.getId(), e);
        failedRepositoryNames.add(gitRepository.getName());
      }
    }
    if (!failedRepositoryNames.isEmpty()) {
      throw new FailedSyncGitChangeNotesException(String.format("Failed to synchronize %d of %d Git repositories: %s",
          failedRepositoryNames.size(), gitRepositories.size(), String.join(", ", failedRepositoryNames)));
    }
  }
  

  /*
   * Synchronizes change notes from a Git repository.
   *
   * This is done using the following steps:
   * <ul>
   * <li> Prepares local directory for Git repositories
   * <li> Prepare the Git repository
   * <li> Synchronize change notes from the Git repository
   * </ul>
   */
  public void syncGitRepository(GitRepository gitRepository) throws Exception {

    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (gitRepository.getChangeNoteDirectory() == null || gitRepository.getChangeNoteDirectory().isBlank()) {
      throw new IllegalStateException("No change note directory configured for Git repository " + gitRepository.getName());
    }

    File repositoriesDirectory = new File(REPOSITORY_DIRECTORIES_PATH);
    if (!repositoriesDirectory.exists()) {
      repositoriesDirectory.mkdirs();
    }

    logger.info("Updating Git repository {} using change note directory: {}", gitRepository.getName(), gitRepository.getChangeNoteDirectory());
    File repositoryDirectory = new File(gitRepository.getLocalPath());
    prepareGitRepository(gitRepository, repositoryDirectory);
    syncFromGitRepository(gitRepository, repositoryDirectory);
  }

  /**
   * Prepares a Git repository by cloning it if it does not exist locally, or updating it from the remote if it does.
   * 
   * A new local directory is created if it is not already present.
   * The last checked commit hash is wiped if the repository is being cloned, ensuring that the persisted data is correct.
   * 
   * @param gitRepository the Git repository to prepare, must not be null
   * @param repositoryDirectory, the local directory for the repository, must not be null
   */
  private void prepareGitRepository(GitRepository gitRepository, File repositoryDirectory) {
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }
    
    if (!repositoryDirectory.exists()) {
      repositoryDirectory.mkdirs();
    }
    File gitDir = new File(repositoryDirectory, ".git");
    if (!gitDir.exists()) {
      gitRepository.setLastCheckedCommitHash(null);
      gitRepositoryRepository.save(gitRepository);
      cloneRepository(gitRepository, repositoryDirectory);
    } else {
      fetchAndResetRepository(gitRepository, repositoryDirectory);
    }
  }
  
  /**
   * Clones a Git repository to a local directory.
   *
   * @param gitRepository the Git repository to clone, must not be null
   * @param repositoryDirectory the local directory for the repository, must not be null
   */
  private void cloneRepository(GitRepository gitRepository, File repositoryDirectory) {
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }
    
    logger.info("Cloning repository {} with id {}", gitRepository.getName(), gitRepository.getId());
    try (Git git = Git.cloneRepository()
        .setURI(gitRepository.getUrl())
        .setDirectory(repositoryDirectory)
        .call()) {
    } catch (Exception e) {
      throw new FailedSyncGitChangeNotesException("Failed to clone Git repository " + gitRepository.getName(), e);
    }
  }
  
  /**
   * Updates a Git repository by fetching and hard-resetting the checked-out branch to its remote tracking branch, discarding any local-only changes.
   *
   * @param gitRepository the Git repository to update, must not be null
   * @param repositoryDirectory the local directory for the repository, must not be null
   */
  private void fetchAndResetRepository(GitRepository gitRepository, File repositoryDirectory) {
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }

    try (Git git = Git.open(repositoryDirectory);) {
      RepositoryState repositoryState = git.getRepository().getRepositoryState();
      if (repositoryState != RepositoryState.SAFE) {
        logger.warn("Repository {} with id {} is in state {}. Resetting it to recover", gitRepository.getName(), gitRepository.getId(), repositoryState);
        git.reset().setMode(ResetType.HARD).call();
      }
      logger.info("Fetching repository {} with id {}", gitRepository.getName(), gitRepository.getId());
      git.fetch().call();
      String trackingBranch = new BranchConfig(git.getRepository().getConfig(), git.getRepository().getBranch()).getTrackingBranch();
      if (trackingBranch == null) {
        checkoutDefaultBranch(git, gitRepository);
        trackingBranch = new BranchConfig(git.getRepository().getConfig(), git.getRepository().getBranch()).getTrackingBranch();
      }
      if (trackingBranch == null) {
        throw new FailedSyncGitChangeNotesException("No remote tracking branch configured for the checked-out branch in Git repository " + gitRepository.getName());
      }
      git.reset().setMode(ResetType.HARD).setRef(trackingBranch).call();
    } catch (FailedSyncGitChangeNotesException e) {
      throw e;
    } catch (Exception e) {
      throw new FailedSyncGitChangeNotesException("Failed to update Git repository " + gitRepository.getName() + " from remote", e);
    }
  }

  /**
   * Force-checks out the default branch (main or master) of a Git repository.
   *
   * @param git the open Git handle, must not be null
   * @param gitRepository the Git repository entity, must not be null
   */
  private void checkoutDefaultBranch(Git git, GitRepository gitRepository) throws GitAPIException {
    Set<String> branchNames = git.branchList().call().stream()
        .map(Ref::getName)
        .collect(Collectors.toSet());
    String defaultBranch;
    if (branchNames.contains("refs/heads/main")) {
      defaultBranch = "main";
    } else if (branchNames.contains("refs/heads/master")) {
      defaultBranch = "master";
    } else {
      throw new FailedSyncGitChangeNotesException("No default branch (main or master) found in Git repository " + gitRepository.getName());
    }
    logger.warn("Repository {} with id {} has no remote tracking branch for the checked-out branch. Checking out {}", gitRepository.getName(), gitRepository.getId(), defaultBranch);
    git.checkout().setName(defaultBranch).setForced(true).call();
  }
  
  /**
   * Synchronizes change notes from a Git repository by checking for new commits since the last checked commit, and creating change notes from any new change note files found in those commits.
   *
   * The last checked commit hash is updated after processing the commits, ensuring that only new commits are processed in the next synchronization.
   * Any trailing commits behind the last change note commit are not considered for being marked as last checked, and will be re-checked in the next synchronization.
   * @param gitRepository the Git repository to synchronize, must not be null
   * @param repositoryDirectory the local directory for the repository, must not be null
   */
  private void syncFromGitRepository(GitRepository gitRepository, File repositoryDirectory) {
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }
    
    try (Git git = Git.open(repositoryDirectory);
    RevWalk revWalk = new RevWalk(git.getRepository());) {
      Repository repository = git.getRepository();
      if (gitRepository.getLastCheckedCommitHash() != null) {
        ObjectId lastCheckedCommitId = repository.resolve(gitRepository.getLastCheckedCommitHash()); 
        RevCommit lastCheckedCommit = revWalk.parseCommit(lastCheckedCommitId);       
        revWalk.markUninteresting(lastCheckedCommit); // only keep commits after the last checked commit
      }
      revWalk.markStart(revWalk.parseCommit(repository.resolve(Constants.HEAD)));
      revWalk.sort(RevSort.TOPO);
      revWalk.sort(RevSort.REVERSE); // sort commits from oldest to newest
      ObjectId lastCheckedCommitId = createChangeNotesFromCommits(revWalk, repositoryDirectory, repository, gitRepository);
      if (lastCheckedCommitId != null) {
        updateLastCheckedCommitHash(gitRepository, lastCheckedCommitId);
      }
    } catch (IOException e) {
      throw new FailedSyncGitChangeNotesException("Failed to open Git repository " + gitRepository.getName(), e);
    }
  }
  
  /**
   * Creates change notes from commits in a Git repository by checking for new change note files in the commits and creating change notes from those files.
   * 
   * If multiple change note files are found in the same commit, only the first one will be processed.
   * The created change note entities are persisted in the database.
   * 
   * @param revWalk the RevWalk to iterate over the commits, must not be null, must be ordered from oldest to newest
   * @param repositoryDirectory the local directory for the repository, must not be null
   * @param repository the JGit Repository object, must not be null
   * @param gitRepository the Git repository entity, must not be null
   * @return the ObjectId of the last commit that was processed for change notes, or null if no commits with change notes were found
   */
  private ObjectId createChangeNotesFromCommits(RevWalk revWalk, File repositoryDirectory, Repository repository, GitRepository gitRepository) {
    if (revWalk == null) {
      throw new IllegalArgumentException("RevWalk cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }
    if (repository == null) {
      throw new IllegalArgumentException("Repository cannot be null");
    }
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    

    RevCommit lastCheckedCommit = null;
    try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);) {
      diffFormatter.setRepository(repository);

      for (RevCommit commit : revWalk) {
        if (commit.getParentCount() == 0) {
          logger.warn("No parent commit for commit {} with message '{}'. Skipping diff", commit.getName(), commit.getShortMessage());
          continue;
        }

        RevCommit parentCommit = revWalk.parseCommit(commit.getParent(0).getId());
        List<DiffEntry> diffEntries = diffFormatter.scan(parentCommit.getTree(), commit.getTree());
        List<File> newChangeNoteFiles = diffEntries.stream()
          .filter(diffEntry -> diffEntry.getChangeType() == DiffEntry.ChangeType.ADD)
          .filter(diffEntry -> diffEntry.getNewPath().startsWith(gitRepository.getChangeNoteDirectory() + "/"))
          .filter(diffEntry -> diffEntry.getNewPath().endsWith(".yaml") || diffEntry.getNewPath().endsWith(".yml"))
          .map(diffEntry -> new File(repositoryDirectory, diffEntry.getNewPath()))
          .toList();

        if (newChangeNoteFiles.isEmpty()) {
          logger.debug("No new change note files found in commit {} with message '{}'", commit.getName(), commit.getShortMessage());
        } else {
          if (newChangeNoteFiles.size() > 1) {
            logger.warn("Found multiple new change note files for commit {}: {}. Only the first will be processed", commit.getName(), newChangeNoteFiles.stream().map(File::getPath).toList());
          }
          File changeNoteFile = newChangeNoteFiles.get(0);
          ChangeNote changeNote = getChangeNoteFromFile(changeNoteFile);
          if (changeNote != null) {
            changeNote.setGitRepository(gitRepository);
            changeNote.setGitCommitHash(commit.getName());
            changeNote.setGitCommitTimestamp(Instant.ofEpochSecond(commit.getCommitTime())); // git commit time is epoch seconds
            try {
              changeNoteService.updateChangeNote(changeNote);
              logger.info("Created change note from file {} for commit {} in repository with id {}", changeNoteFile.getPath(), commit.getName(), gitRepository.getId());
            } catch (DataIntegrityViolationException e) {
              logger.warn("Failed to create change note from file {} for commit {} in repository with id {} due to data integrity violation. This is likely caused by a duplicate git commit hash. Skipping this change note file", changeNoteFile.getPath(), commit.getName(), gitRepository.getId());
            }
          }
        }
        lastCheckedCommit = commit;
      }
      
    } catch (IOException e) {
      throw new FailedSyncGitChangeNotesException("Failed to check new commits for Git repository " + gitRepository.getName(), e);
    }

    if (lastCheckedCommit == null) {
      logger.warn("No commits with change notes found in repository at {}", repositoryDirectory.getPath());
      return null;
    }
    return lastCheckedCommit.getId();
    
  }
  
  /**
   * Wraps the call to the method for creating a ChangeNote entity from a change note file
   *
   * @param changeNoteFile the file containing the change note, must not be null
   * @return the ChangeNote entity, or null if the file is invalid or cannot be parsed
   */
  private ChangeNote getChangeNoteFromFile(File changeNoteFile) {
    if (changeNoteFile == null) {
      throw new IllegalArgumentException("Change note file cannot be null");
    }
    ChangeNote changeNote = null;
    try {
      changeNote = changeNoteFileHandler.getChangeNoteFromFile(changeNoteFile);
    } catch (InvalidChangeNoteYamlException e) {
      logger.warn("Failed to parse change note file at {} due to invalid YAML format. Reason: {}. Skipping this change note file", changeNoteFile.getPath(), e.getReason());
    }
    return changeNote;
  }
  
  /**
   * Updates the last checked commit hash for a Git repository after processing commits for change notes. This ensures that only new commits will be processed in the next synchronization.
   * 
   * @param gitRepository the Git repository entity, must not be null
   * @param newLastCheckedCommitId the ObjectId of the new last checked commit, must not be null
   */
  private void updateLastCheckedCommitHash(GitRepository gitRepository, ObjectId newLastCheckedCommitId) {
    if (newLastCheckedCommitId == null) {
      throw new IllegalArgumentException("New last checked commit ID cannot be null");
    }
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    gitRepository.setLastCheckedCommitHash(newLastCheckedCommitId.getName());
    gitRepositoryRepository.save(gitRepository);
    logger.info("Updated last checked commit hash for repository with id {} to {}", gitRepository.getId(), newLastCheckedCommitId.getName());
    
  }
  
  
  
  
  
  
}

