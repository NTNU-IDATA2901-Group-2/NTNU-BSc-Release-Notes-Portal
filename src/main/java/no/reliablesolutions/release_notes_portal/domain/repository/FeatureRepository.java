package no.reliablesolutions.release_notes_portal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
  
}
