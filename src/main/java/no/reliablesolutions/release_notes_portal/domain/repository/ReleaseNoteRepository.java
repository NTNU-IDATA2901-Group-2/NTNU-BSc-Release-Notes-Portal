package no.reliablesolutions.release_notes_portal.domain.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;

/**
 * Repository for managing ReleaseNote entities.
 */
public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Long> {

  /**
   * Finds all non-archived release notes that match the optional provided filter
   * parameters.
   * 
   * @param query      The search query to match against the tag and summary
   *                   fields (case-insensitive).
   * @param published  The filter for published status
   * @param productIds The list of product IDs to filter by.
   * @param includeUnassignedProduct Whether to include release notes that are not associated with any product
   * 
   * @return all non-archived release notes that match the optional provided filter
   * parameters
   */
  @Query("""
      SELECT DISTINCT r
      FROM ReleaseNote r
      LEFT JOIN r.changeNotes c
        ON c.archived = false
      WHERE r.archived = false AND
        (:published IS NULL OR r.published = :published) AND
        ((:productIds IS NULL AND :includeUnassignedProduct IS NULL)
          OR (:productIds IS NOT NULL AND c.product.id IN :productIds)
          OR (:includeUnassignedProduct IS NOT NULL AND c.product IS NULL)) AND
        ((:query IS NULL OR :query = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :query || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :query || '%')) AND
        (:fromDate IS NULL OR r.createdAt >= :fromDate) AND
        (:toDate IS NULL OR r.createdAt <= :toDate)
      ORDER BY r.createdAt DESC
      """)
  public List<ReleaseNote> findByArchivedFalseAndMatchingFilterParameters(String query, Boolean published,
      List<Long> productIds, Boolean includeUnassignedProduct, Long fromDate, Long toDate);

  /**
   * Finds all non-archived release notes that match the optional provided filter
   * parameters.
   * 
   * @param query      The search query to match against the tag and summary
   *                   fields (case-insensitive).
   * @param published  The filter for published status
   * @param productIds The list of product IDs to filter by.
   * @param includeUnassignedProduct Whether to include release notes that are not associated with any product
   * @param customerGroups The list of customer groups to filter by (case-insensitive).
   * 
   * @return all non-archived release notes that match the optional provided filter
   * parameters
   */
  @Query("""
      SELECT DISTINCT r
      FROM ReleaseNote r
      LEFT JOIN r.changeNotes c ON c.archived = false
        AND (c.customer IS NULL OR UPPER( c.customer.name ) IN :customerGroups)
      WHERE r.archived = false AND
        (:published IS NULL OR r.published = :published) AND
        ((:productIds IS NULL AND :includeUnassignedProduct IS NULL)
          OR (:productIds IS NOT NULL AND c.product.id IN :productIds)
          OR (:includeUnassignedProduct IS NOT NULL AND c.product IS NULL)) AND
        ((:query IS NULL OR :query = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :query || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :query || '%')) AND
        (:fromDate IS NULL OR r.createdAt >= :fromDate) AND
        (:toDate IS NULL OR r.createdAt <= :toDate)
      ORDER BY r.createdAt DESC
      """)
  public List<ReleaseNote> findByArchivedFalseAndMatchingFilterParametersForCustomers(String query, Boolean published,
      List<Long> productIds, Boolean includeUnassignedProduct, Long fromDate, Long toDate, List<String> customerGroups);
}
