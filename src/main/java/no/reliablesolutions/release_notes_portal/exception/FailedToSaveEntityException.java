package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when an entity fails to save to the database.
 */
public class FailedToSaveEntityException extends RuntimeException {

  /**
   * Constructs a new FailedToSaveEntityException with the specified detail message.
   *
   * @param message the detail message
   */
  public FailedToSaveEntityException(String message) {
    super(message);
  }

  /**
   * Constructs a new FailedToSaveEntityException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the underlying cause of the failure
   */
  public FailedToSaveEntityException(String message, Throwable cause) {
    super(message, cause);
  }
}
