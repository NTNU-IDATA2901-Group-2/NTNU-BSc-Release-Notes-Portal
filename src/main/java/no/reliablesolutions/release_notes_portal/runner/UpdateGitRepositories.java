package no.reliablesolutions.release_notes_portal.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.service.GitRepositoryService;

@Component
@AllArgsConstructor
public class UpdateGitRepositories implements CommandLineRunner {

  private final Logger logger = LoggerFactory.getLogger(UpdateGitRepositories.class);
  private final GitRepositoryService gitRepositoryService;

  private static final String REPOSITORY_DIRECTORIES_PATH = "git_repositories";
  private static final String CHANGE_NOTE_DIRECTORY = "notes";

  @Override
  public void run(String... args) throws Exception {

    File repositoriesDirectory = new File(REPOSITORY_DIRECTORIES_PATH);

    if (!repositoriesDirectory.exists()) {
      repositoriesDirectory.mkdirs();
    }

    List<GitRepository> gitRepositories = gitRepositoryService.getAllGitRepositories();
    logger.info("Found {} git repositories", gitRepositories.size());
    gitRepositories.forEach(gitRepository -> {
      logger.info("Updating Git repository: {}", gitRepository.getName());

      File repositoryDirectory = new File(REPOSITORY_DIRECTORIES_PATH + File.separator + gitRepository.getName());

      prepareGitRepository(gitRepository, repositoryDirectory);
      updateGitRepository(gitRepository, repositoryDirectory);
    });
  }

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
      gitRepositoryService.updateGitRepository(gitRepository);
      cloneRepository(gitRepository, repositoryDirectory);
    } else {
      pullRepository(gitRepository, repositoryDirectory);
    }
  }

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

  private void updateGitRepository(GitRepository gitRepository, File repositoryDirectory) {
    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }

    try (Git git = Git.open(repositoryDirectory);
	Repository repository = git.getRepository();
	RevWalk revWalk = new RevWalk(repository);) {
      RevCommit headCommit = revWalk.parseCommit(repository.resolve(Constants.HEAD));
      RevCommit lastCheckedCommit;
      if (gitRepository.getLastCheckedCommitHash() != null) {
	lastCheckedCommit = revWalk.parseCommit(repository.resolve(gitRepository.getLastCheckedCommitHash()));
      } else {
	revWalk.sort(RevSort.REVERSE);
	revWalk.markStart(headCommit);
	lastCheckedCommit = revWalk.next(); // oldest commit
      }
      doDiffLogic(lastCheckedCommit, headCommit, repositoryDirectory);
      updateLastCheckedCommitHash(gitRepository, headCommit);
    } catch (IOException e) {
      logger.error("Failed to open repository with id {} due to IO error", gitRepository.getId(), e);
    } 


  }

  private void doDiffLogic(RevCommit from, RevCommit to, File repositoryDirectory) {
    if (from == null) {
      throw new IllegalArgumentException("From commit cannot be null");
    }
    if (to == null) {
      throw new IllegalArgumentException("To commit cannot be null");
    }
    if (repositoryDirectory == null) {
      throw new IllegalArgumentException("Repository directory cannot be null");
    }
    logger.info("Performing diff logic between commits {} and {}", from.getName(), to.getName());
    try (Git git = Git.open(repositoryDirectory);
	Repository repository = git.getRepository();
      DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
	) {
      diffFormatter.setRepository(repository);
      List<DiffEntry> diffEntries = diffFormatter.scan(from.getTree(), to.getTree());
      List<File> newChangeNoteFiles = diffEntries.stream()
	.filter(diffEntry -> diffEntry.getChangeType() == DiffEntry.ChangeType.ADD)
	.filter(diffEntry -> diffEntry.getNewPath().startsWith(CHANGE_NOTE_DIRECTORY + "/"))
	.map(diffEntry -> new File(repositoryDirectory, diffEntry.getNewPath()))
	.toList();
      logger.info("Found {} new change note files", newChangeNoteFiles.size());
      newChangeNoteFiles.forEach(file -> {
        try {

	BufferedReader br = new BufferedReader(new FileReader(file));
	String line;
	while ((line = br.readLine()) != null) {
	  logger.info("Read line from change note file {}: {}", file.getPath(), line);
	}
	} catch (IOException e) {
	  logger.error("Failed to read change note file at {} due to IO error", file.getPath(), e);
	} 
      });
    } catch (IOException e) {
      logger.error("Failed to perform diff logic for repository at {} due to IO error", repositoryDirectory.getPath(), e);
    }

  }

  private void updateLastCheckedCommitHash(GitRepository gitRepository, ObjectId newLastCheckedCommit) {
    if (newLastCheckedCommit == null) {
      logger.error("Failed to resolve new last checked commit for repository with id {}", gitRepository.getId());
    } else {
      gitRepository.setLastCheckedCommitHash(newLastCheckedCommit.getName());
      gitRepositoryService.updateGitRepository(gitRepository);
      logger.info("Updated last checked commit hash for repository with id {} to {}", gitRepository.getId(), newLastCheckedCommit.getName());
    }
  }






}

