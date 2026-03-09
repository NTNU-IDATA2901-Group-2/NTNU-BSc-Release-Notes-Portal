package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

@Getter
public class ChangeNoteAlreadyHasReleaseNoteException extends RuntimeException {
  final long changeNoteId;
  final long existingReleaseNoteId;

  public ChangeNoteAlreadyHasReleaseNoteException(long changeNoteId, long existingReleaseNoteId) {
    super("Change note with id " + changeNoteId + " already has a release note with id: " + existingReleaseNoteId);
    this.changeNoteId = changeNoteId;
    this.existingReleaseNoteId = existingReleaseNoteId;
  }
}
