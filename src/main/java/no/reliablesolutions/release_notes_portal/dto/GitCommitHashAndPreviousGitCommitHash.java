package no.reliablesolutions.release_notes_portal.dto;

/**
 * An interface representing a Git commit hash and its previous Git commit hash.
 */
public interface GitCommitHashAndPreviousGitCommitHash {

  /**
   * Gets the latest Git commit hash.
   *
   * @return the Git commit hash
   */
  String getGitCommitHash();

  /**
   * Gets the previous Git commit hash.
   *
   * @return the previous Git commit hash
   */
  String getPreviousGitCommitHash();
}
