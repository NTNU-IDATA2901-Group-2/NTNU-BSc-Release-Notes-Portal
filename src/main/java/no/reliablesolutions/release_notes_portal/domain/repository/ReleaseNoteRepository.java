package no.reliablesolutions.release_notes_portal.domain.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteFilterOptionsDTO;

/**
 * Repository for managing ReleaseNote entities.
 */
public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Long> {

  /**
   * Finds all non-archived release notes that match the optional provided filter
   * parameters.
   *
   * @param filterOptions the filter options to apply to the search
   * @param fromDate      the lower bound (inclusive) for the release note creation
   *                      timestamp, or {@code null} for no lower bound
   * @param toDate        the upper bound (exclusive) for the release note creation
   *                      timestamp, or {@code null} for no upper bound
   * @param pageable      the pagination information for the query
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
        (:#{#filterOptions.published} IS NULL OR r.published = :#{#filterOptions.published}) AND
        ((:#{#filterOptions.productIds} IS NULL AND :#{#filterOptions.includeUnassignedProduct} IS NULL)
          OR (:#{#filterOptions.productIds} IS NOT NULL AND c.product.id IN :#{#filterOptions.productIds})
          OR (:#{#filterOptions.includeUnassignedProduct} IS NOT NULL AND c.product IS NULL)) AND
        ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :#{#filterOptions.query} || '%')) AND
        (CAST(:fromDate AS Instant) IS NULL OR r.createdAt >= :fromDate) AND
        (CAST(:toDate AS Instant) IS NULL OR r.createdAt < :toDate)
      ORDER BY r.createdAt DESC
      """)
  public Page<ReleaseNote> findByArchivedFalseAndMatchingFilterParameters(
      @Param("filterOptions") ReleaseNoteFilterOptionsDTO filterOptions,
      @Param("fromDate") Instant fromDate,
      @Param("toDate") Instant toDate,
      Pageable pageable);

  /**
   * Finds all non-archived release notes that match the optional provided filter
   * parameters.
   *
   * @param filterOptions  the filter options to apply to the search
   * @param fromDate       the lower bound (inclusive) for the release note creation
   *                       timestamp, or {@code null} for no lower bound
   * @param toDate         the upper bound (exclusive) for the release note creation
   *                       timestamp, or {@code null} for no upper bound
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
        (:#{#filterOptions.published} IS NULL OR r.published = :#{#filterOptions.published}) AND
        ((:#{#filterOptions.productIds} IS NULL AND :#{#filterOptions.includeUnassignedProduct} IS NULL)
          OR (:#{#filterOptions.productIds} IS NOT NULL AND c.product.id IN :#{#filterOptions.productIds})
          OR (:#{#filterOptions.includeUnassignedProduct} IS NOT NULL AND c.product IS NULL)) AND
        ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :#{#filterOptions.query} || '%')) AND
        (CAST(:fromDate AS Instant) IS NULL OR r.createdAt >= :fromDate) AND
        (CAST(:toDate AS Instant) IS NULL OR r.createdAt < :toDate)
      ORDER BY r.createdAt DESC
      """)
  public Page<ReleaseNote> findByArchivedFalseAndMatchingFilterParametersForCustomers(
      @Param("filterOptions") ReleaseNoteFilterOptionsDTO filterOptions,
      @Param("fromDate") Instant fromDate,
      @Param("toDate") Instant toDate,
      @Param("customerGroups") List<String> customerGroups,
      Pageable pageable);
}
