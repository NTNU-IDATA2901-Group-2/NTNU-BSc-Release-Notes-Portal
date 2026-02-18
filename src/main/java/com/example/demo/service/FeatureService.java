package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.Feature;
import com.example.demo.domain.repository.FeatureRepository;
import com.example.demo.dto.FeatureDTO;
import com.example.demo.exception.FeatureNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FeatureService {
  private final FeatureRepository featureRepository;

  /**
   * Creates a new feature based on the provided DTO.
   *
   * @param featureDTO the DTO containing details for the new feature
   * @return the ID of the created feature
   */
  public long createFeature(FeatureDTO featureDTO) {
    Feature feature = new Feature();
    feature.setName(featureDTO.name());
    return featureRepository.save(feature).getId();
  }

  /**
   * Retrieves all features from the repository.
   *
   * @return a list of all features
   */
  public List<FeatureDTO> getAllFeatures() {
    return featureRepository.findAll().stream()
        .map(FeatureDTO::fromFeature)
        .toList();
  }

  /**
   * Retrieves a specific feature by its ID.
   *
   * @param id the ID of the feature to retrieve
   * @return a DTO representing the feature
   * @throws FeatureNotFoundException if no feature with the given ID exists
   */
  public FeatureDTO getFeatureById(long id) {
    Feature feature = featureRepository.findById(id)
        .orElseThrow(() -> new FeatureNotFoundException(id));
    return FeatureDTO.fromFeature(feature);
  }

  /**
   * Updates an existing feature with new details from the provided DTO.
   *
   * @param id the ID of the feature to update
   * @param featureDTO the DTO containing updated details for the feature
   * @return a DTO representing the updated feature
   * @throws FeatureNotFoundException if no feature with the given ID exists
   */
  public FeatureDTO updateFeature(long id, FeatureDTO featureDTO) {
    Feature feature = featureRepository.findById(id)
        .orElseThrow(() -> new FeatureNotFoundException(id));
    feature.setName(featureDTO.name());
    return FeatureDTO.fromFeature(featureRepository.save(feature));
  }

  /**
   * Deletes an existing feature by its ID.
   *
   * @param id the ID of the feature to delete
   * @throws FeatureNotFoundException if no feature with the given ID exists
   */
  public void deleteFeature(long id) {
    Feature feature = featureRepository.findById(id)
        .orElseThrow(() -> new FeatureNotFoundException(id));
    featureRepository.delete(feature);
  }
}
