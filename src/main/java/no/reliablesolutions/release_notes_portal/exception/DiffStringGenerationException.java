package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when there is an error during the generation of a diff string.
 */
public class DiffStringGenerationException extends RuntimeException {
  
  /**
   * Constructor for DiffStringGenerationException.
   *
   * @param message the detail message for the exception
   */
  public DiffStringGenerationException(String message) {
    super(message);
  }
}
