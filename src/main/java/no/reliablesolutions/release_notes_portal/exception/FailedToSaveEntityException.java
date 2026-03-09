package no.reliablesolutions.release_notes_portal.exception;

public class FailedToSaveEntityException extends RuntimeException {
  public FailedToSaveEntityException(String message) {
    super(message);
  }
}
