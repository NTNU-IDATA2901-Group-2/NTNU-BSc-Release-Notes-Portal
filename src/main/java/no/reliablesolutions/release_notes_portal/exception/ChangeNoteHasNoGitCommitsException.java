package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when a change note has no associated git commits.
 */
public class ChangeNoteHasNoGitCommitsException extends RuntimeException {

  /**
   * Constructor for ChangeNoteHasNoGitCommitsException.
   * @param changeNoteId the ID of the change note that has no associated git commits
   */
  public ChangeNoteHasNoGitCommitsException(long changeNoteId) {
    super("Change note with ID " + changeNoteId + " has no associated git commits.");
  }
}
