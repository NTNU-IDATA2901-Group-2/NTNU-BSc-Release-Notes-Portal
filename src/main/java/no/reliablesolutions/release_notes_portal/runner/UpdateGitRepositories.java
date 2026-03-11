package no.reliablesolutions.release_notes_portal.runner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
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

    try (Git git = Git.open(repositoryDirectory);) {
      Iterable<RevCommit> commitsSinceLastUpdate;
      if (gitRepository.getLastCheckedCommitHash() == null) {
        logger.info("No last checked commit hash for repository with id {}, retrieving entire commit log", gitRepository.getId());
        commitsSinceLastUpdate = git.log().call();
      } else {
        commitsSinceLastUpdate = git.log().addRange(
            git.getRepository().resolve(gitRepository.getLastCheckedCommitHash()),
            git.getRepository().resolve(Constants.HEAD)
          ).call();
      }

      if (!commitsSinceLastUpdate.iterator().hasNext()) {
        logger.info("No new commits found for repository with id {}", gitRepository.getId());
      } else  {
        commitsSinceLastUpdate.forEach(this::doCommitLogic);
        updateLastCheckedCommitHash(gitRepository, git.getRepository().resolve(Constants.HEAD));
      }
    } catch (IOException e) {
      logger.error("Failed to open repository with id {} due to IO error", gitRepository.getId(), e);
    } catch (GitAPIException e) {
      logger.error("Failed to access repository with id {} due to Git API error", gitRepository.getId(), e);
    }


  }

  private void doCommitLogic(RevCommit commit) {
    logger.info("Commit: " + commit.getName());
    logger.info("Author: " + commit.getAuthorIdent().getName());
    logger.info("Date: " + commit.getAuthorIdent().getWhenAsInstant());
    logger.info("Message: " + commit.getFullMessage());
    logger.info("----------------------------------");
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

