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
        (:published IS NULL OR c.published = :published) AND
        (:customerIds IS NULL OR c.customer.id IN :customerIds) AND
        (:featureIds IS NULL OR c.feature.id IN :featureIds) AND
        (:scopeIds IS NULL OR c.scope.id IN :scopeIds) AND
        (:productIds IS NULL OR c.product.id IN :productIds) AND
        ((:query IS NULL OR :query = '') OR
        LOWER(c.reference) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.description) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.developerNotes) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.upgradeNotes) LIKE LOWER('%' || :query || '%') OR
        LOWER(c.changeSource) LIKE LOWER('%' || :query || '%'))
      
      """)
    public List<ChangeNote> findByArchivedFalseAndMatchingFilterParameters(String query, Boolean published, List<Long> customerIds, List<Long> featureIds, List<Long> scopeIds, List<Long> productIds);
}
