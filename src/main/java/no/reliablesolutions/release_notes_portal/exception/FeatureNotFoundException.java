package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a feature with the specified ID is not found.
 */
@Getter
public class FeatureNotFoundException extends RuntimeException {
  final Long featureId;

  /**
   * Constructs a new FeatureNotFoundException with the specified feature ID.
   *
   * @param featureId the ID of the feature that was not found
   */
  public FeatureNotFoundException(Long featureId) {
    super("Feature with ID " + featureId + " not found");
    this.featureId = featureId;
  }
}
