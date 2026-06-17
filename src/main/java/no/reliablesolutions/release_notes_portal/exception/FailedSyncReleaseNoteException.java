package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when synchronizing a release note to its Git repository fails,
 * covering both the local commit and the push to the remote.
 */
public class FailedSyncReleaseNoteException extends RuntimeException {

  /**
   * Constructs a new FailedSyncReleaseNoteException with the specified detail message.
   *
   * @param message the detail message explaining the reason for the failure
   */
  public FailedSyncReleaseNoteException(String message) {
    super(message);
  }

  /**
   * Constructs a new FailedSyncReleaseNoteException with the specified detail message and cause.
   *
   * @param message the detail message explaining the reason for the failure
   * @param cause the underlying exception that triggered this failure
   */
  public FailedSyncReleaseNoteException(String message, Throwable cause) {
    super(message, cause);
  }
}
