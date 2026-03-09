package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Feature;

public record FeatureDTO(
  long id,
  String name
) {
  public static FeatureDTO fromFeature(Feature feature) {
    return new FeatureDTO(feature.getId(), feature.getName());
  }
}
