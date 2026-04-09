package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;

public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Long> {

  /**
   * Finds all non-archived release notes that match the optional provided filter
   * parameters.
   * 
   * @param query      The search query to match against the tag and summary
   *                   fields (case-insensitive).
   * @param published  The filter for published status
   * @param productIds The list of product IDs to filter by.
   */
  @Query("""
      SELECT r
      FROM ReleaseNote r
      LEFT JOIN r.changeNotes c
        ON c.archived = false
      WHERE r.archived = false AND
        (:published IS NULL OR r.published = :published) AND
        (:productIds IS NULL OR c.product.id IN :productIds) AND
        ((:query IS NULL OR :query = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :query || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :query || '%'))
      """)
  public List<ReleaseNote> findByArchivedFalseAndMatchingFilterParameters(String query, Boolean published,
      List<Long> productIds);

  /**
   * Finds all non-archived release notes that match the optional provided filter
   * parameters.
   * 
   * @param query      The search query to match against the tag and summary
   *                   fields (case-insensitive).
   * @param published  The filter for published status
   * @param productIds The list of product IDs to filter by.
   */
  @Query("""
      SELECT r
      FROM ReleaseNote r
      LEFT JOIN r.changeNotes c ON c.archived = false
        AND (c.customer IS NULL OR UPPER( c.customer.name ) IN :customerGroups)
      WHERE r.archived = false AND
        (:published IS NULL OR r.published = :published) AND
        (:productIds IS NULL OR c.product.id IN :productIds) AND
        ((:query IS NULL OR :query = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :query || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :query || '%'))
      """)
  public List<ReleaseNote> findByArchivedFalseAndMatchingFilterParametersForCustomers(String query, Boolean published,
      List<Long> productIds, List<String> customerGroups);
}
