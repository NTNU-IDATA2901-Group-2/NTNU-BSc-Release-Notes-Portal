package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a change note YAML file is invalid or cannot be parsed.
 * 
 * A reason for the invalidity is provided in the exception message.
 */
@Getter
public class InvalidChangeNoteYamlException extends RuntimeException  {
  private final String reason;

  /**
   * Creates a new InvalidChangeNoteYamlException with the given reason for the invalidity of the change note YAML file.
   *
   * @param reason the reason why the change note YAML file is invalid
   */
  public InvalidChangeNoteYamlException(String reason) {
    this.reason = reason;
    super("Invalid change note YAML: " + reason);
  }
}
