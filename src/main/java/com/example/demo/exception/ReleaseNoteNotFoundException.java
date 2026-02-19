package com.example.demo.exception;

import lombok.Getter;

/**
 * Custom exception thrown when a release note with a specified ID is not found.
 */
@Getter
public class ReleaseNoteNotFoundException extends RuntimeException {
  final long releaseNoteId;

  /**
   * Constructs a new ReleaseNoteNotFoundException with the specified release note ID.
   * @param id the ID of the release note that was not found
   */
  public ReleaseNoteNotFoundException(long id) {
    super("Release note not found with id: " + id);
    this.releaseNoteId = id;
  }
}
