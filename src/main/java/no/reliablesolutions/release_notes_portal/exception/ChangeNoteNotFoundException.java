package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a change note with the specified ID is not found.
 */
@Getter
public class ChangeNoteNotFoundException extends RuntimeException {
  final long changeNoteId;

  /**
   * Constructs a new ChangeNoteNotFoundException with the specified change note ID.
   *
   * @param id the ID of the change note that was not found
   */
  public ChangeNoteNotFoundException(long id) {
    super("Change note not found with id: " + id);
    this.changeNoteId = id;
  }
}
