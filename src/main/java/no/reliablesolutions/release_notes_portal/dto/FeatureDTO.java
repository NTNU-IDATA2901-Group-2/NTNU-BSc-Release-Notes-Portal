package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Feature;

/**
 * A data transfer object for representing a feature.
 */
public record FeatureDTO(
    long id,
    String name) {

  /**
   * Creates a FeatureDTO from a Feature entity.
   *
   * @param feature the Feature entity
   * @return the FeatureDTO
   */
  public static FeatureDTO fromFeature(Feature feature) {
    return new FeatureDTO(feature.getId(), feature.getName());
  }
}
