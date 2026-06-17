package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when attempting to sync a release note to Git that has already been synced.
 */
public class ReleaseNoteAlreadySyncedException extends RuntimeException {

  /**
   * Creates a new ReleaseNoteAlreadySyncedException.
   *
   * @param releaseNoteId the ID of the release note that has already been synced to Git
   */
  public ReleaseNoteAlreadySyncedException(long releaseNoteId) {
    super("Release note with ID " + releaseNoteId + " has already been synced to Git.");
  }
}
