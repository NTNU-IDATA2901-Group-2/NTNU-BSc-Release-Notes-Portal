package no.reliablesolutions.release_notes_portal.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.exception.GitInspectionException;

/**
 * Service for generating diff strings between git commits.
 * This service is only active in non-CI profiles, as it relies on local git
 * repositories being available.
 */
@Service
@Profile("!ci")
public class ChangeNoteGitInspectionService {
  private final Logger logger = LoggerFactory.getLogger(ChangeNoteGitInspectionService.class);
  private final String changeNoteDirectory;

  /**
   * Constructor for ChangeNoteGitInspectionService.
   *
   * @param repositoryDirectoriesPath the base path where local git repositories
   *                                  are stored, injected from application
   *                                  properties
   */
  public ChangeNoteGitInspectionService(
      @Value("${CHANGE_NOTE_DIRECTORY}") String changeNoteDirectory) {
    this.changeNoteDirectory = changeNoteDirectory;
  }

  /**
   * Generates a diff string between two commits for a given git repository.
   *
   * @param commitHash         the hash of the new commit
   * @param previousCommitHash the hash of the previous commit
   * @param gitRepository      the git repository for which the diff is to be
   *                           generated
   * @param filePath           the path of the file to generate the diff for, or
   *                           null to generate the diff for all files
   * @return a string representation of the diff between the two commits
   * @throws IllegalArgumentException if any of the parameters are null or if the
   *                                  repository directory does not exist
   * @throws GitInspectionException   if there is an error while generating the
   *                                  diff string
   */
  @Tool(name = "generateDiffString", description = "Generates a diff string between two commits for a given git repository. Optionally, a specific file path can be provided to generate the diff for that file only.")
  public String getDiffString(String commitHash, String previousCommitHash, GitRepository gitRepository,
      String filePath)
      throws GitInspectionException {
    if (commitHash == null || previousCommitHash == null) {
      throw new IllegalArgumentException("Commit hashes cannot be null");
    }

    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }

    File repositoryDirectory = new File(gitRepository.getLocalPath());
    if (!repositoryDirectory.exists()) {
      throw new IllegalArgumentException(
          "Repository directory does not exist: " + repositoryDirectory.getAbsolutePath());
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
        if (diff.getNewPath().startsWith(changeNoteDirectory)
            || filePath != null && !diff.getNewPath().equals(filePath)) {
          continue;
        }
        diffFormatter.format(diff);
      }
      diffStringBuilder.append(outputStream.toString());
    } catch (Exception e) {
      throw new GitInspectionException(String.format(
          "Error generating diff string for commit %s and previous commit %s in repository %s: %s",
          commitHash, previousCommitHash, gitRepository.getName(), e.getMessage()), e);
    }

    logger.info("Generated diff string for commit {} and previous commit {} in repository {}",
        commitHash, previousCommitHash, gitRepository.getName());
    return diffStringBuilder.toString();
  }

  /**
   * Retrieves the full commit message for a given commit in a git repository.
   *
   * @param commitHash    the hash of the commit whose message is to be retrieved
   * @param gitRepository the git repository the commit belongs to
   * @return the full commit message
   * @throws IllegalArgumentException if any of the parameters are null or if the
   *                                  repository directory does not exist
   * @throws GitInspectionException   if there is an error while retrieving the
   *                                  commit message
   */
  @Tool(name = "getCommitMessage", description = "Retrieves the full commit message for a given commit in a git repository")
  public String getCommitMessage(String commitHash, GitRepository gitRepository) {
    if (commitHash == null) {
      throw new IllegalArgumentException("Commit hash cannot be null");
    }

    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }

    File repositoryDirectory = new File(gitRepository.getLocalPath());
    if (!repositoryDirectory.exists()) {
      throw new IllegalArgumentException(
          "Repository directory does not exist: " + repositoryDirectory.getAbsolutePath());
    }

    try (Git git = Git.open(repositoryDirectory);
        Repository repository = git.getRepository()) {

      RevCommit commit = repository.parseCommit(repository.resolve(commitHash));
      return commit.getFullMessage();
    } catch (Exception e) {
      throw new GitInspectionException(String.format(
          "Error retrieving commit message for commit %s in repository %s: %s",
          commitHash, gitRepository.getName(), e.getMessage()), e);
    }
  }

  /**
   * Retrieves the commit messages for the commits in the range
   * {@code startCommitHash..endCommitHash}, i.e. commits reachable from
   * {@code endCommitHash} but not from {@code startCommitHash}.
   *
   * @param startCommitHash the hash of the older boundary commit (exclusive)
   * @param endCommitHash   the hash of the newer boundary commit (inclusive)
   * @param gitRepository   the git repository the commits belong to
   * @return a string containing the commit messages in the range, newest first,
   *         separated by newlines; empty if the range contains no commits
   * @throws IllegalArgumentException if any of the parameters are null or if the
   *                                  repository directory does not exist
   * @throws GitInspectionException   if there is an error while retrieving the
   *                                  commit messages
   */
  @Tool(name = "getCommitMessagesBetweenRange", description = "Retrieves the commit messages for all commits between two specified commits in a git repository")
  public String getCommitMessageBetweenRange(String startCommitHash, String endCommitHash,
      GitRepository gitRepository) {
    if (startCommitHash == null || endCommitHash == null) {
      throw new IllegalArgumentException("Start and end commit hashes cannot be null");
    }

    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }

    File repositoryDirectory = new File(gitRepository.getLocalPath());
    if (!repositoryDirectory.exists()) {
      throw new IllegalArgumentException(
          "Repository directory does not exist: " + repositoryDirectory.getAbsolutePath());
    }

    try (Git git = Git.open(repositoryDirectory);
        Repository repository = git.getRepository();
        RevWalk walk = new RevWalk(repository);) {

      RevCommit startCommit = walk.parseCommit(repository.resolve(startCommitHash));
      RevCommit endCommit = walk.parseCommit(repository.resolve(endCommitHash));

      walk.markStart(endCommit);
      walk.markUninteresting(startCommit);

      StringBuilder commitMessages = new StringBuilder();
      for (RevCommit commit : walk) {
        commitMessages.append(commit.getFullMessage()).append("\n");
      }
      return commitMessages.toString().trim();
    } catch (Exception e) {
      throw new GitInspectionException(String.format(
          "Error retrieving commit messages between %s and %s in repository %s: %s",
          startCommitHash, endCommitHash, gitRepository.getName(), e.getMessage()), e);
    }
  }

  /**
   * Retrieves the content of a file at a specific commit in a git repository.
   *
   * @param commitHash    the hash of the commit
   * @param filePath      the path of the file relative to the repository root
   * @param gitRepository the git repository the file belongs to
   * @return the content of the file as a string
   * @throws IllegalArgumentException if any of the parameters are null or if the
   *                                  repository directory does not exist
   * @throws GitInspectionException   if the file does not exist at the specified
   *                                  commit or if there is an error while
   *                                  retrieving the file content
   */
  @Tool(name = "getFileAtCommit", description = "Retrieves the content of a file at a specific commit in a git repository")
  public String getFileAtCommit(String commitHash, String filePath, GitRepository gitRepository) {
    if (commitHash == null || filePath == null) {
      throw new IllegalArgumentException("Commit hash and file path cannot be null");
    }

    if (gitRepository == null) {
      throw new IllegalArgumentException("Git repository cannot be null");
    }

    File repositoryDirectory = new File(gitRepository.getLocalPath());
    if (!repositoryDirectory.exists()) {
      throw new IllegalArgumentException(
          "Repository directory does not exist: " + repositoryDirectory.getAbsolutePath());
    }

    try (Git git = Git.open(repositoryDirectory);
        Repository repository = git.getRepository();
        TreeWalk treeWalk = new TreeWalk(repository);) {

      RevCommit commit = repository.parseCommit(repository.resolve(commitHash));
      RevTree tree = commit.getTree();
      treeWalk.addTree(tree);
      treeWalk.setRecursive(true);
      treeWalk.setFilter(PathFilter.create(filePath));

      if (!treeWalk.next()) {
        throw new GitInspectionException(String.format(
            "File %s does not exist at commit %s in repository %s",
            filePath, commitHash, gitRepository.getName()));
      }
      ObjectId blobId = treeWalk.getObjectId(0);
      ObjectLoader loader = repository.open(blobId);
      logger.info("Retrieved file {} at commit {} in repository {}", filePath, commitHash, gitRepository.getName());
      return new String(loader.getBytes());

    } catch (Exception e) {
      throw new GitInspectionException(String.format(
          "Error retrieving file %s at commit %s in repository %s: %s",
          filePath, commitHash, gitRepository.getName(), e.getMessage()), e);
    }
  }

  /**
   * Retrieves the list of files changed between a commit and its previous commit
   * in a git repository. Paths within the change note directory are excluded.
   *
   * @param commitHash         the hash of the (newer) commit
   * @param previousCommitHash the hash of the previous commit to diff against
   * @param gitRepository      the git repository the commits belong to
   * @return the changed file paths separated by newlines, or a message stating
   *         that no files changed when the range is empty
   * @throws IllegalArgumentException if any parameter is null, the repository
   *                                  directory does not exist, or a commit hash
   *                                  cannot be resolved
   * @throws GitInspectionException   if there is an error while retrieving the
   *                                  changed files
   */
  @Tool(name = "getChangedFilesInCommit", description = "Lists the files changed between a commit and its previous commit, one path per line")
  public String getChangedFilesInCommit(String commitHash, String previousCommitHash, GitRepository gitRepository) {
    if (commitHash == null || previousCommitHash == null || gitRepository == null) {
      throw new IllegalArgumentException("Commit hashes and git repository cannot be null");
    }

    File repositoryDirectory = new File(gitRepository.getLocalPath());
    if (!repositoryDirectory.exists()) {
      throw new IllegalArgumentException(
          "Repository directory does not exist: " + repositoryDirectory.getAbsolutePath());
    }

    try (Git git = Git.open(repositoryDirectory)) {
      Repository repository = git.getRepository();

      ObjectId oldTreeId = repository.resolve(previousCommitHash + "^{tree}");
      ObjectId newTreeId = repository.resolve(commitHash + "^{tree}");
      if (oldTreeId == null || newTreeId == null) {
        throw new IllegalArgumentException("Could not resolve commit hashes to tree objects: " + previousCommitHash + ", " + commitHash);
      }

      try (ObjectReader reader = repository.newObjectReader()) {
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldTreeId);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newTreeId);

        List<DiffEntry> diffs = git.diff()
            .setOldTree(oldTreeIter)
            .setNewTree(newTreeIter)
            .call()
            .stream()
            .filter(diff -> !diff.getNewPath().startsWith(changeNoteDirectory))
            .toList();

        logger.info("Found {} changed files between {} and {} in repository {}",
            diffs.size(), previousCommitHash, commitHash, gitRepository.getName());
        return diffs.stream()
            .map(DiffEntry::getNewPath)
            .reduce((a, b) -> a + "\n" + b)
            .orElse("No files changed between the provided commits.");
      }
    } catch (Exception e) {
      throw new GitInspectionException(String.format(
          "Error retrieving changed files between %s and %s in repository %s: %s",
          previousCommitHash, commitHash, gitRepository.getName(), e.getMessage()), e);
    }

  }
}
