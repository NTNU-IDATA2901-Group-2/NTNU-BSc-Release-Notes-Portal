package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when synchronization of Git change notes fails.
 */
public class FailedSyncGitChangeNotesException extends RuntimeException {

  /**
   * Constructs a new FailedSyncGitChangeNotesException with the specified detail message.
   * @param message the detail message explaining the reason for the failure
   */
  public FailedSyncGitChangeNotesException(String message) {
    super(message);
  }
}
