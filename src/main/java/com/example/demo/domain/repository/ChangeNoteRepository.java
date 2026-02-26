package com.example.demo.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.domain.entity.ChangeNote;

public interface ChangeNoteRepository extends JpaRepository<ChangeNote, Long> {
  List<ChangeNote> findByArchivedFalse();
  Optional<ChangeNote> findByIdAndArchivedFalse(Long id);


  @Query("""
      SELECT c
      FROM ChangeNote c

      WHERE c.archived = false AND
        (:published IS NULL OR :published = c.published) AND
        (:customerId IS NULL OR :customerId = c.customer.id) AND
        (:featureId IS NULL OR :featureId = c.feature.id) AND
        (:scopeId IS NULL OR :scopeId = c.scope.id) AND
        (:productId IS NULL OR :productId = c.product.id) AND
        ((:query IS NULL OR :query = '') OR
        LOWER(c.reference) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.description) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.developerNotes) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.upgradeNotes) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.changeSource) LIKE LOWER('%' || :query || '%'))
      
      """)
    public List<ChangeNote> findByArchivedFalseAndMatchingFilterParameters(String query, Boolean published, Long customerId, Long featureId, Long scopeId, Long productId);
}
