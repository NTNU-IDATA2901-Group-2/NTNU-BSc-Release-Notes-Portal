package no.reliablesolutions.release_notes_portal.runner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import jakarta.persistence.Transient;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.exception.InvalidChangeNoteYamlException;
import no.reliablesolutions.release_notes_portal.service.ChangeNoteService;
import no.reliablesolutions.release_notes_portal.util.ChangeNoteFileHandler;

/**
 * This class is responsible for synchronizing change notes from Git repositories. It will clone or pull the repositories, check for new commits, and create change notes from any new change note files found in the commits.
 */
@Component
public class SyncGitChangeNotes implements CommandLineRunner {
  
  private final Logger logger = LoggerFactory.getLogger(SyncGitChangeNotes.class);
  private final GitRepositoryRepository gitRepositoryRepository;
  private final ChangeNoteService changeNoteService;
  private final ChangeNoteFileHandler changeNoteFileHandler;
  
  private final String repositoryDirectoriesPath;
  private final String changeNoteDirectory;

  public SyncGitChangeNotes(
    GitRepositoryRepository gitRepositoryRepository,
    ChangeNoteService changeNoteService,
    ChangeNoteFileHandler changeNoteFileHandler,
    @Value("${REPOSITORY_DIRECTORIES_PATH}") String repositoryDirectoriesPath,
    @Value("${CHANGE_NOTE_DIRECTORY}") String changeNoteDirectory
  ) {
    this.gitRepositoryRepository = gitRepositoryRepository;
    this.changeNoteService = changeNoteService;
    this.changeNoteFileHandler = changeNoteFileHandler;
    this.repositoryDirectoriesPath = repositoryDirectoriesPath;
    this.changeNoteDirectory = changeNoteDirectory;
  }

  /**
   * Runs the synchronization process for Git change notes on all Git repositories.
   */
  @Override
  public void run(String... args) throws Exception {    
    List<GitRepository> gitRepositories = gitRepositoryRepository.findAll();
    logger.info("Found {} git repositories", gitRepositories.size());
    gitRepositories.forEach(gitRepository -> {
      try {
        this.syncGitRepository(gitRepository);
      } catch (Exception e) {
        logger.error("Failed to synchronize Git repository with id {} due to unexpected error", gitRepository.getId(), e);
      }
    });
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

    File repositoriesDirectory = new File(repositoryDirectoriesPath);
    if (!repositoriesDirectory.exists()) {
      repositoriesDirectory.mkdirs();
    }

    logger.info("Updating Git repository: {}", gitRepository.getName());
    File repositoryDirectory = new File(gitRepository.getLocalPath(repositoryDirectoriesPath));
    prepareGitRepository(gitRepository, repositoryDirectory);
    syncFromGitRepository(gitRepository, repositoryDirectory);
  }

  /**
   * Prepares a Git repository by cloning it if it does not exist locally, or pulling the latest changes if it does.
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
      pullRepository(gitRepository, repositoryDirectory);
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
    
    try {
      logger.info("Cloning repository {} with id {}", gitRepository.getName(), gitRepository.getId());
      Git.cloneRepository()
      .setURI(gitRepository.getUrl())
      .setDirectory(repositoryDirectory) 
      .call();
    } catch (GitAPIException e) {
      logger.error("Failed to clone repository with id {}", gitRepository.getId(), e);
    } catch (Exception e) {
      logger.error("Failed to clone repository with id {} due to unexpected error", gitRepository.getId(), e);
    }
  }
  
  /**
   * Pulls the latest changes for a Git repository from the remote.
   *
   * @param gitRepository the Git repository to pull, must not be null
   * @param repositoryDirectory the local directory for the repository, must not be null
   */
  private void pullRepository(GitRepository gitRepository, File repositoryDirectory) {
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }
    
    try (Git git = Git.open(repositoryDirectory);) {
      logger.info("Pulling repository {} with id {}", gitRepository.getName(), gitRepository.getId());
      git.pull().call();
    } catch (Exception e) {
      logger.error("Failed to pull repository with id {} due to unexpected error", gitRepository.getId(), e);
    }
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
    Repository repository = git.getRepository();
    RevWalk revWalk = new RevWalk(repository);) {
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
      logger.error("Failed to open repository with id {} due to IO error", gitRepository.getId(), e);
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
          .filter(diffEntry -> diffEntry.getNewPath().startsWith(changeNoteDirectory + File.separator))
          .filter(diffEntry -> diffEntry.getNewPath().endsWith(".yaml") || diffEntry.getNewPath().endsWith(".yml"))
          .map(diffEntry -> new File(repositoryDirectory, diffEntry.getNewPath()))
          .toList();

        if (!newChangeNoteFiles.isEmpty()) {
          if (newChangeNoteFiles.size() > 1) {
            logger.warn("Found multiple new change note files for commit {}: {}. Only the first will be processed", commit.getName(), newChangeNoteFiles.stream().map(File::getPath).toList());
          }
          File changeNoteFile = newChangeNoteFiles.get(0);
          ChangeNote changeNote = getChangeNoteFromFile(changeNoteFile);
          if (changeNote != null) {
            changeNote.setGitRepository(gitRepository);
            changeNote.setGitCommitHash(commit.getName());
            changeNote.setGitCommitTimestamp(commit.getCommitTime() * 1000L); // convert seconds to milliseconds
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
      logger.error("Failed to check new commits for repository at {} due to IO error", repositoryDirectory.getPath(), e);
    }
    
    if (lastCheckedCommit == null) {
      logger.warn("No commits with change notes found in repository at {}", repositoryDirectory.getPath());
      return null;
    }
    return lastCheckedCommit.getId();
    
  }
  
  /**
   * Wraps the call to the method for creating a ChangeNote entity from a change note file
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

