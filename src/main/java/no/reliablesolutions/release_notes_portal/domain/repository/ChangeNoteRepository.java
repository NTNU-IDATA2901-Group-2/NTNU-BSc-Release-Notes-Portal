package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;



public interface ChangeNoteRepository extends JpaRepository<ChangeNote, Long> {
  List<ChangeNote> findByArchivedFalse();
  Optional<ChangeNote> findByIdAndArchivedFalse(Long id);


  /**
   * Finds all non-archived change notes that match the provided filter parameters.
   * 
   * @param query          optional filter for searching change notes by reference, description, developer notes, upgrade notes, or change source
   * @param published      optional filter for published status
   * @param hasReleaseNote optional filter for change notes that have an associated release note
   * @param customerIds    optional filter for customer ID
   * @param featureIds     optional filter for feature ID
   * @param scopeIds       optional filter for scope ID
   * @param productIds     optional filter for product ID
   * 
   * @return a list of all non-archived change notes that match the provided filters
   */
  @Query("""
      SELECT c
      FROM ChangeNote c
      LEFT JOIN c.releaseNote r ON (r.archived = false)
      WHERE c.archived = false AND
        (:published IS NULL OR c.published = :published) AND
        (:hasReleaseNote IS NULL OR
          (:hasReleaseNote = TRUE AND c.releaseNote IS NOT NULL) OR
          (:hasReleaseNote = FALSE AND c.releaseNote IS NULL)
        ) AND
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
    public List<ChangeNote> findByArchivedFalseAndMatchingFilterParameters(String query, Boolean published, Boolean hasReleaseNote, List<Long> customerIds, List<Long> featureIds, List<Long> scopeIds, List<Long> productIds);
}