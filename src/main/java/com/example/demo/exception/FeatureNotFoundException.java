package com.example.demo.exception;

import lombok.Getter;

@Getter
public class FeatureNotFoundException extends RuntimeException {
  final Long featureId;

  public FeatureNotFoundException(Long featureId) {
    super("Feature with ID " + featureId + " not found");
    this.featureId = featureId;
  }
}
