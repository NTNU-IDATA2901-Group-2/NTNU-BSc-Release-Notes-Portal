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
}
