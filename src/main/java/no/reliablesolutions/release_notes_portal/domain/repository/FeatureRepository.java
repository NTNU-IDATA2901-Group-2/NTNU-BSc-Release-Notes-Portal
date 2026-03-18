package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Long> {

  List<Feature> findAllByNameIgnoreCase(String feature);
  
}
