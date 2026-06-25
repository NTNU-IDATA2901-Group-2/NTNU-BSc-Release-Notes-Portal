package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when two release notes that are being compared do not belong
 * to the same product.
 */
@Getter
public class MismatchedProductException extends RuntimeException {
  final long releaseNoteOneId;
  final long releaseNoteTwoId;

  /**
   * Constructs a new MismatchedProductException for the two release notes whose
   * products did not match.
   *
   * @param releaseNoteOneId the ID of the first release note
   * @param releaseNoteTwoId the ID of the second release note
   */
  public MismatchedProductException(long releaseNoteOneId, long releaseNoteTwoId) {
    super("Release notes " + releaseNoteOneId + " and " + releaseNoteTwoId
        + " do not belong to the same product");
    this.releaseNoteOneId = releaseNoteOneId;
    this.releaseNoteTwoId = releaseNoteTwoId;
  }
}
