package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteFilterOptionsDTO;
import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;

/**
 * Repository for managing ChangeNote entities.
 */
public interface ChangeNoteRepository extends JpaRepository<ChangeNote, Long> {

  /**
   * Finds all non-archived change notes.
   * @return a list of all non-archived change notes
   */
  List<ChangeNote> findByArchivedFalse();

  /**
   * Finds a non-archived change note by its ID.
   *
   * @param id the ID of the change note to find
   * @return an Optional containing the found change note, or an empty Optional if no non-archived change note with the given ID exists
   */
  Optional<ChangeNote> findByIdAndArchivedFalse(Long id);

  /**
   * Finds a non-archived change note by its ID, if it is viewable by everyone or associated with a customer whose name is in the provided list.
   *
   * @param id the ID of the change note to find
   * @param customerNames a list of customer names to filter by (case-insensitive)
   * @return an Optional containing the found change note, or an empty Optional if no matching change note is found
   */
  @Query("""
      SELECT c
      FROM ChangeNote c
      LEFT JOIN c.customer customer
      WHERE c.archived = false
      AND (c.viewableByEveryone = true OR customer IS NULL OR UPPER( customer.name ) IN :customerNames)
      AND c.id = :id
      """)
  Optional<ChangeNote> findForCustomerByIdAndArchivedFalse(Long id, List<String> customerNames);

  /**
   * Finds all non-archived change notes that match the provided filter
   * parameters.
   * 
   * @param filterOptions the filter options to apply to the search
   * @return a list of all non-archived change notes that match the provided
   *         filters
   */
  @Query("""
      SELECT c
      FROM ChangeNote c
      WHERE c.archived = false AND
        (:#{#filterOptions.published} IS NULL OR c.published = :#{#filterOptions.published}) AND
        (:#{#filterOptions.hasReleaseNote} IS NULL OR
          (:#{#filterOptions.hasReleaseNote} = TRUE AND EXISTS (SELECT 1 FROM c.releaseNotes rn WHERE rn.archived = false)) OR
          (:#{#filterOptions.hasReleaseNote} = FALSE AND NOT EXISTS (SELECT 1 FROM c.releaseNotes rn WHERE rn.archived = false))
        ) AND
        (:#{#filterOptions.filteredIds} IS NULL OR c.id IN :#{#filterOptions.filteredIds}) AND
        (
          (:#{#filterOptions.customerIds} IS NULL AND :#{#filterOptions.includeUnassignedCustomer} IS NULL)
          OR (:#{#filterOptions.customerIds} IS NOT NULL AND c.customer.id IN :#{#filterOptions.customerIds})
          OR (:#{#filterOptions.includeUnassignedCustomer} IS NOT NULL AND c.customer IS NULL)
        ) AND
        (
          (:#{#filterOptions.featureIds} IS NULL AND :#{#filterOptions.includeUnassignedFeature} IS NULL)
          OR (:#{#filterOptions.featureIds} IS NOT NULL AND c.feature.id IN :#{#filterOptions.featureIds})
          OR (:#{#filterOptions.includeUnassignedFeature} IS NOT NULL AND c.feature IS NULL)
        ) AND
        (
          (:#{#filterOptions.scopeIds} IS NULL AND :#{#filterOptions.includeUnassignedScope} IS NULL)
          OR (:#{#filterOptions.scopeIds} IS NOT NULL AND c.scope.id IN :#{#filterOptions.scopeIds})
          OR (:#{#filterOptions.includeUnassignedScope} IS NOT NULL AND c.scope IS NULL)
        ) AND
        (
          (:#{#filterOptions.productIds} IS NULL AND :#{#filterOptions.includeUnassignedProduct} IS NULL)
          OR (:#{#filterOptions.productIds} IS NOT NULL AND c.product.id IN :#{#filterOptions.productIds})
          OR (:#{#filterOptions.includeUnassignedProduct} IS NOT NULL AND c.product IS NULL)
        ) AND
        (:#{#filterOptions.gitRepositoryIds} IS NULL OR c.gitRepository.id IN :#{#filterOptions.gitRepositoryIds}) AND
        ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '') OR
        LOWER(c.title) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.reference) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.description) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.developerNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.upgradeNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%')) AND
        (:fromDate IS NULL OR COALESCE(c.gitCommitTimestamp, c.creationTimestamp) >= :fromDate) AND
        (:toDate IS NULL OR COALESCE(c.gitCommitTimestamp, c.creationTimestamp) <= :toDate)
      ORDER BY CASE WHEN c.gitCommitTimestamp IS NULL THEN c.creationTimestamp ELSE c.gitCommitTimestamp END DESC
      """)
  public List<ChangeNote> findByArchivedFalseAndMatchingFilterParameters(
      @Param("filterOptions") ChangeNoteFilterOptionsDTO filterOptions,
      @Param("fromDate") Long fromDate,
      @Param("toDate") Long toDate);

  /**
   * Finds all non-archived change notes that are viewable by everyone or
   * associated with a customer whose name is in the provided list, and that match
   * the provided filter parameters.
   * 
   * @param customerNames a list of customer names to filter by (case-insensitive)
   * @param filterOptions the filter options to apply to the search
   * @return a list of all non-archived change notes that are viewable by everyone
   *         or associated with a customer whose name is in the provided list, and
   *         that match the provided filter parameters
   */
  @Query("""
      SELECT c
      FROM ChangeNote c
      LEFT JOIN c.customer customer
      WHERE c.archived = false
      AND (
        c.viewableByEveryone = true
        OR (customer IS NULL OR UPPER( customer.name ) IN :customerNames)
      )
      AND (:#{#filterOptions.published} IS NULL OR c.published = :#{#filterOptions.published})
      AND (:#{#filterOptions.hasReleaseNote} IS NULL OR
            (:#{#filterOptions.hasReleaseNote} = TRUE AND EXISTS (SELECT 1 FROM c.releaseNotes rn WHERE rn.archived = false)) OR
            (:#{#filterOptions.hasReleaseNote} = FALSE AND NOT EXISTS (SELECT 1 FROM c.releaseNotes rn WHERE rn.archived = false))
          )
      AND (:#{#filterOptions.filteredIds} IS NULL OR c.id IN :#{#filterOptions.filteredIds})
      AND (
        (:#{#filterOptions.customerIds} IS NULL AND :#{#filterOptions.includeUnassignedCustomer} IS NULL)
        OR (:#{#filterOptions.customerIds} IS NOT NULL AND c.customer.id IN :#{#filterOptions.customerIds})
        OR (:#{#filterOptions.includeUnassignedCustomer} IS NOT NULL AND c.customer IS NULL)
      )
      AND (
        (:#{#filterOptions.featureIds} IS NULL AND :#{#filterOptions.includeUnassignedFeature} IS NULL)
        OR (:#{#filterOptions.featureIds} IS NOT NULL AND c.feature.id IN :#{#filterOptions.featureIds})
        OR (:#{#filterOptions.includeUnassignedFeature} IS NOT NULL AND c.feature IS NULL)
      )
      AND (
        (:#{#filterOptions.scopeIds} IS NULL AND :#{#filterOptions.includeUnassignedScope} IS NULL)
        OR (:#{#filterOptions.scopeIds} IS NOT NULL AND c.scope.id IN :#{#filterOptions.scopeIds})
        OR (:#{#filterOptions.includeUnassignedScope} IS NOT NULL AND c.scope IS NULL)
      )
      AND (
        (:#{#filterOptions.productIds} IS NULL AND :#{#filterOptions.includeUnassignedProduct} IS NULL)
        OR (:#{#filterOptions.productIds} IS NOT NULL AND c.product.id IN :#{#filterOptions.productIds})
        OR (:#{#filterOptions.includeUnassignedProduct} IS NOT NULL AND c.product IS NULL)
      )
      AND (:#{#filterOptions.gitRepositoryIds} IS NULL OR c.gitRepository.id IN :#{#filterOptions.gitRepositoryIds})
      AND ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '')
      OR LOWER(c.title) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.reference) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.description) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.developerNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.upgradeNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%'))
      AND (:fromDate IS NULL OR COALESCE(c.gitCommitTimestamp, c.creationTimestamp) >= :fromDate)
      AND (:toDate IS NULL OR COALESCE(c.gitCommitTimestamp, c.creationTimestamp) <= :toDate)
      ORDER BY CASE WHEN c.gitCommitTimestamp IS NULL THEN c.creationTimestamp ELSE c.gitCommitTimestamp END DESC
      """)
  public List<ChangeNote> findForCustomerNamesMatchingFilterParameters(
      @Param("customerNames") List<String> customerNames,
      @Param("filterOptions") ChangeNoteFilterOptionsDTO filterOptions,
      @Param("fromDate") Long fromDate,
      @Param("toDate") Long toDate);

  /**
   * Finds the Git commit hash and the previous Git commit hash for a change note
   * with the specified ID, if it is not archived and has a Git commit hash.
   *
   * @param changeNoteId the ID of the change note to find the Git commit hash and
   *                     previous Git commit hash for
   * @return a GitCommitHashAndPreviousGitCommitHash object with the Git commit
   *         hash and previous Git commit hash
   */
  @Query("""
      SELECT c.gitCommitHash AS gitCommitHash,
        (SELECT c2.gitCommitHash
        FROM ChangeNote c2
        WHERE c2.gitRepository.id = c.gitRepository.id
          AND c2.gitCommitTimestamp IS NOT NULL
          AND c2.gitCommitTimestamp < c.gitCommitTimestamp
          AND c2.archived = false
        ORDER BY c2.gitCommitTimestamp DESC
        LIMIT 1) AS previousGitCommitHash
      FROM ChangeNote c
      WHERE c.id = :changeNoteId
        AND c.archived = false
        AND c.gitCommitHash IS NOT NULL
      """)
  public GitCommitHashAndPreviousGitCommitHash findCommitHashAndPreviousCommitHash(Long changeNoteId);


  /**
   * Checks if any of the change notes with the specified IDs are not archived, have a Git commit hash, and have a previous Git commit hash.
   * @param changeNoteIds a list of change note IDs to check
   * @return true if any of the specified change notes meet the criteria, false otherwise
   */
  @Query("""
      SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
      FROM ChangeNote c
      WHERE c.id IN :changeNoteIds
        AND c.archived = false
        AND c.gitCommitHash IS NOT NULL
        AND EXISTS (SELECT 1
        FROM ChangeNote c2 
        WHERE c2.gitRepository.id = c.gitRepository.id 
          AND c2.gitCommitTimestamp IS NOT NULL
          AND c2.gitCommitTimestamp < c.gitCommitTimestamp
          AND c2.archived = false)
      """)
  public boolean hasCommitHashAndPreviousCommitHash(List<Long> changeNoteIds);

  /**
   * Clears all references to the Git repository with the specified ID from any associated change notes.
   *
   * @param gitRepositoryId the ID of the Git repository for which to clear references
   */
  @Query("""
      UPDATE ChangeNote c
      SET c.gitRepository = null
      WHERE c.gitRepository.id = :gitRepositoryId
      """)
  @Transactional
  @Modifying
  void clearGitRepositoryReferencesById(long gitRepositoryId);
}