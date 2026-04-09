package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Long> {

  /**
   * Finds all features with the specified name, ignoring case.
   *
   * @param feature the name of the feature to find
   * @return a list of features with the specified name (case-insensitive)
   */
  List<Feature> findAllByNameIgnoreCase(String feature);

}
