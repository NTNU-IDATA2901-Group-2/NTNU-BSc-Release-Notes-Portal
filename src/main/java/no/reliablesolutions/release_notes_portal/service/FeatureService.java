package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.Feature;
import no.reliablesolutions.release_notes_portal.domain.repository.FeatureRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.FeatureDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.FeatureNotFoundException;

/**
 * Service for managing feature-related operations.
 */
@Service
@AllArgsConstructor
public class FeatureService {
  private final FeatureRepository featureRepository;

  /**
   * Creates a new feature based on the provided DTO.
   *
   * @param featureDTO the DTO containing details for the new feature
   * @return the ID of the created feature
   * @throws FailedToSaveEntityException if there was an error saving the feature to the repository
   */
  public long createFeature(CreateTagDTO featureDTO) {
    Feature feature = new Feature();
    feature.setName(featureDTO.name());
    try {
      return featureRepository.save(feature).getId();
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to create feature");
    }
  }

  /**
   * Retrieves all features from the repository as FeatureDTOs.
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
   * @throws FailedToSaveEntityException if there was an error saving the updated feature to the repository
   */
  public FeatureDTO updateFeature(long id, CreateTagDTO featureDTO) {
    Feature feature = featureRepository.findById(id)
        .orElseThrow(() -> new FeatureNotFoundException(id));
    feature.setName(featureDTO.name());
    try {
      return FeatureDTO.fromFeature(featureRepository.save(feature));
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to update feature with ID " + id);
    }
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

  /**
    * Retrieves a list of features that match the provided name.
    * 
    * @param feature the name to search for
    * @return A list of features that match the provided name. If no features match, returns an empty list.
    */
  public List<Feature> getFeatureByName(String feature) {
    return featureRepository.findAllByNameIgnoreCase(feature);
  }
}
