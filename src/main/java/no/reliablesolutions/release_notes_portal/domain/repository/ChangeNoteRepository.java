package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteFilterOptionsDTO;

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
   * @param query          optional filter for searching change notes by reference, description, developer notes or upgrade notes
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
        ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '') OR
        LOWER(c.reference) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.description) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.developerNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%') OR
        LOWER(c.upgradeNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%'))
      """)
  public List<ChangeNote> findByArchivedFalseAndMatchingFilterParameters(
      @Param("filterOptions") ChangeNoteFilterOptionsDTO filterOptions);

  @Query("""
      SELECT c
      FROM ChangeNote c
      LEFT JOIN c.customer customer
      WHERE c.archived = false
      AND (customer IS NULL OR UPPER( customer.name ) IN :customerNames)
      """)
  public List<ChangeNote> findForCustomerNames(List<String> customerNames);

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
      AND ((:#{#filterOptions.query} IS NULL OR :#{#filterOptions.query} = '')
      OR LOWER(c.reference) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.description) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.developerNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%')
      OR LOWER(c.upgradeNotes) LIKE LOWER('%' || :#{#filterOptions.query} || '%'))
      """)
  public List<ChangeNote> findForCustomerNamesMatchingFilterParameters(
      @Param("customerNames") List<String> customerNames,
      @Param("filterOptions") ChangeNoteFilterOptionsDTO filterOptions);
}