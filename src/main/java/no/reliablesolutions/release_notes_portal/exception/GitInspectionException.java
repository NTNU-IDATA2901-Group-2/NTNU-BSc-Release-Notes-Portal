package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when a git inspection operation fails, such as generating a diff,
 * retrieving a commit message, or listing the files changed in a commit.
 */
public class GitInspectionException extends RuntimeException {

  /**
   * Constructor for GitInspectionException.
   *
   * @param message the detail message for the exception
   */
  public GitInspectionException(String message) {
    super(message);
  }

  /**
   * Constructor for GitInspectionException that preserves the underlying cause.
   *
   * @param message the detail message for the exception
   * @param cause the underlying exception that triggered this one
   */
  public GitInspectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
