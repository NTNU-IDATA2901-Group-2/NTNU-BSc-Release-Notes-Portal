package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteFilterOptionsDTO;
import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;

public interface ChangeNoteRepository extends JpaRepository<ChangeNote, Long> {
  List<ChangeNote> findByArchivedFalse();

  Optional<ChangeNote> findByIdAndArchivedFalse(Long id);

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
   * @param query          optional filter for searching change notes by
   *                       reference, description, developer notes or upgrade
   *                       notes
   * @param published      optional filter for published status
   * @param hasReleaseNote optional filter for change notes that have an
   *                       associated release note
   * @param filteredIds    optional filter for specific change note IDs
   * @param customerIds    optional filter for customer ID
   * @param featureIds     optional filter for feature ID
   * @param scopeIds       optional filter for scope ID
   * @param productIds     optional filter for product ID
   * 
   * @return a list of all non-archived change notes that match the provided
   *         filters
   */
  @Query("""
      SELECT c
      FROM ChangeNote c
      LEFT JOIN c.releaseNotes r ON r.archived = false
      WHERE c.archived = false AND
        (:#{#filterOptions.published} IS NULL OR c.published = :#{#filterOptions.published}) AND
        (:#{#filterOptions.hasReleaseNote} IS NULL OR
          (:#{#filterOptions.hasReleaseNote} = TRUE AND r IS NOT NULL) OR
          (:#{#filterOptions.hasReleaseNote} = FALSE AND r IS NULL)
        ) AND
        (:#{#filterOptions.filteredIds} IS NULL OR c.id IN :#{#filterOptions.filteredIds}) AND
        (:#{#filterOptions.customerIds} IS NULL OR c.customer.id IN :#{#filterOptions.customerIds}) AND
        (:#{#filterOptions.featureIds} IS NULL OR c.feature.id IN :#{#filterOptions.featureIds}) AND
        (:#{#filterOptions.scopeIds} IS NULL OR c.scope.id IN :#{#filterOptions.scopeIds}) AND
        (:#{#filterOptions.productIds} IS NULL OR c.product.id IN :#{#filterOptions.productIds}) AND
        (:#{#filterOptions.gitRepositoryIds} IS NULL OR c.gitRepository.id IN :#{#filterOptions.gitRepositoryIds}) AND
        ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '') OR
        LOWER(c.reference) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.description) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.developerNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.upgradeNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%'))
      ORDER BY CASE WHEN c.gitCommitTimestamp IS NULL THEN c.creationTimestamp ELSE c.gitCommitTimestamp END DESC
      """)
  public List<ChangeNote> findByArchivedFalseAndMatchingFilterParameters(
      @Param("filterOptions") ChangeNoteFilterOptionsDTO filterOptions);

  /**
   * Finds all non-archived change notes that are viewable by everyone or
   * associated with a customer whose name is in the provided list.
   * 
   * @param customerNames a list of customer names to filter by (case-insensitive)
   * @return a list of all non-archived change notes that are viewable by everyone
   *         or associated with a customer whose name is in the provided list
   */
  @Query("""
      SELECT c
      FROM ChangeNote c
      LEFT JOIN c.customer customer
      WHERE c.archived = false
      AND (customer IS NULL OR UPPER( customer.name ) IN :customerNames)
      """)
  public List<ChangeNote> findForCustomerNames(List<String> customerNames);

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
      LEFT JOIN c.releaseNotes r ON r.archived = false
      WHERE c.archived = false
      AND (
        c.viewableByEveryone = true
        OR (customer IS NULL OR UPPER( customer.name ) IN :customerNames)
      )
      AND (:#{#filterOptions.published} IS NULL OR c.published = :#{#filterOptions.published})
      AND (:#{#filterOptions.hasReleaseNote} IS NULL OR
            (:#{#filterOptions.hasReleaseNote} = TRUE AND r IS NOT NULL) OR
            (:#{#filterOptions.hasReleaseNote} = FALSE AND r IS NULL)
          )
      AND (:#{#filterOptions.filteredIds} IS NULL OR c.id IN :#{#filterOptions.filteredIds})
      AND (:#{#filterOptions.customerIds} IS NULL OR c.customer.id IN :#{#filterOptions.customerIds})
      AND (:#{#filterOptions.featureIds} IS NULL OR c.feature.id IN :#{#filterOptions.featureIds})
      AND (:#{#filterOptions.scopeIds} IS NULL OR c.scope.id IN :#{#filterOptions.scopeIds})
      AND (:#{#filterOptions.productIds} IS NULL OR c.product.id IN :#{#filterOptions.productIds})
      AND (:#{#filterOptions.gitRepositoryIds} IS NULL OR c.gitRepository.id IN :#{#filterOptions.gitRepositoryIds}) 
      AND ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '')
      OR LOWER(c.reference) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.description) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.developerNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.upgradeNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%'))
      ORDER BY CASE WHEN c.gitCommitTimestamp IS NULL THEN c.creationTimestamp ELSE c.gitCommitTimestamp END DESC
      """)
  public List<ChangeNote> findForCustomerNamesMatchingFilterParameters(
      @Param("customerNames") List<String> customerNames,
      @Param("filterOptions") ChangeNoteFilterOptionsDTO filterOptions);

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

}