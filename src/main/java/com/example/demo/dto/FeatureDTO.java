package com.example.demo.dto;

import com.example.demo.domain.entity.Feature;

public record FeatureDTO(
  long id,
  String name
) {
  public static FeatureDTO fromFeature(Feature feature) {
    return new FeatureDTO(feature.getId(), feature.getName());
  }
}
