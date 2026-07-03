package no.reliablesolutions.release_notes_portal.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.repository.ChangeNoteRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.CustomerRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.FeatureRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.ProductRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.ScopeRepository;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteFilterOptionsDTO;
import no.reliablesolutions.release_notes_portal.dto.CreateChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.PaginatedResponseDTO;
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.CustomerNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.FeatureNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.InvalidDateRangeException;
import no.reliablesolutions.release_notes_portal.exception.ProductNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ScopeNotFoundException;
import no.reliablesolutions.release_notes_portal.util.AccessScope;
import no.reliablesolutions.release_notes_portal.util.AccessScopeFactory;
import no.reliablesolutions.release_notes_portal.util.ChangeNoteMapper;

import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;

/**
 * Service class for managing change notes, including creating, updating, retrieving, and publishing change notes.
 */
@Service
@AllArgsConstructor
public class ChangeNoteService {
  private final Logger logger = LoggerFactory.getLogger(ChangeNoteService.class);
  private final ChangeNoteRepository changeNoteRepository;
  private final ProductRepository productRepository;
  private final ScopeRepository scopeRepository;
  private final FeatureRepository featureRepository;
  private final CustomerRepository customerRepository;

  /** Zone used to resolve a calendar date filter into an absolute instant range. */
  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Oslo");

  /**
   * Creates a new change note based on the provided DTO. Empty fields in the provided DTO remains as default.
   * 
   * @param changeNoteDTO the DTO containing details for the new change note
   * @return the ID of the created change note
   * @throws ProductNotFoundException  if the specified product ID does not exist
   * @throws ScopeNotFoundException    if the specified scope ID does not exist
   * @throws FeatureNotFoundException  if the specified feature ID does not exist
   * @throws CustomerNotFoundException if the specified customer ID does not exist
   */
  public long createChangeNoteFromDto(CreateChangeNoteDTO changeNoteDTO) {
    ChangeNote changeNote = new ChangeNote();

    if (changeNoteDTO != null) {
      changeNote.setTitle(changeNoteDTO.title());
      changeNote.setReference(changeNoteDTO.reference());
      changeNote.setDescription(changeNoteDTO.description());
      changeNote.setDeveloperNotes(changeNoteDTO.developerNotes());
      changeNote.setUpgradeNotes(changeNoteDTO.upgradeNotes());
      
      if (changeNoteDTO.published() != null) {
        changeNote.setPublished(changeNoteDTO.published());
      }

      if (changeNoteDTO.productId() != null) {
        changeNote.setProduct(productRepository.findById(changeNoteDTO.productId())
            .orElseThrow(() -> new ProductNotFoundException(changeNoteDTO.productId())));
      }

      if (changeNoteDTO.scopeId() != null) {
        changeNote.setScope(scopeRepository.findById(changeNoteDTO.scopeId())
            .orElseThrow(() -> new ScopeNotFoundException(changeNoteDTO.scopeId())));
      }

      if (changeNoteDTO.featureId() != null) {
        changeNote.setFeature(featureRepository.findById(changeNoteDTO.featureId())
            .orElseThrow(() -> new FeatureNotFoundException(changeNoteDTO.featureId())));
      }

      if (changeNoteDTO.customerId() != null) {
        changeNote.setCustomer(customerRepository.findById(changeNoteDTO.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(changeNoteDTO.customerId())));
      }

    }

    return changeNoteRepository.save(changeNote).getId();
  }

  /**
   * Updates change note if exists, otherwise saves the provided change note as a new entry in the repository.
   * 
   * @param changeNote change note to be updated or added
   */
  public void updateChangeNote(ChangeNote changeNote) {
    changeNoteRepository.save(changeNote);
  }

  /**
   * Archives a change note by ID
   * 
   * @param id the ID of the change note to archive
   * @throws ChangeNoteNotFoundException if no change note with the given ID
   *                                     exists
   */
  public void archiveChangeNote(long id) {
    ChangeNote changeNote = changeNoteRepository.findById(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));
    changeNote.setArchived(true);
    changeNoteRepository.save(changeNote);
  }

  /**
   * Retrieves non-archived change notes matching the provided filter options,
   * optionally paginated.
   *
   * <p>Admins see notes regardless of published status; non-admins are restricted
   * to published notes and to the customer groups resolved from the current
   * authentication, and have developer and upgrade notes stripped from the result.
   * When {@code page} or {@code size} is {@code null} the result is returned
   * unpaged (all matches in a single page).
   *
   * @param filterOptions optional filter parameters such as query, published
   *                      status, customer ID, feature ID, scope ID, and product
   *                      ID; {@code null} is treated as no filters
   * @param page          the zero-based page index, or {@code null} to return all
   *                      matches unpaged
   * @param size          the page size, or {@code null} to return all matches
   *                      unpaged
   * @return a {@link PaginatedResponseDTO} wrapping the page of ChangeNoteDTOs and
   *         the total item count
   * @throws InvalidDateRangeException if {@code fromDate} is after {@code toDate}
   * @throws IllegalArgumentException  if {@code page} is negative or {@code size}
   *                                   is not positive
   */
  public PaginatedResponseDTO<List<ChangeNoteDTO>> getAllChangeNotes(ChangeNoteFilterOptionsDTO filterOptions, Integer page, Integer size) {

    if (filterOptions == null) {
      filterOptions = new ChangeNoteFilterOptionsDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    LocalDate fromDate = filterOptions.fromDate();
    LocalDate toDate = filterOptions.toDate();

    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new InvalidDateRangeException(fromDate, toDate);
    }

    Pageable pageable;
    if (page == null || size == null) {
      logger.warn("Page number or page size is null. Returning all change notes without pagination.");
      pageable = Pageable.unpaged();
    } else if (page < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    } else if (size <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    } else {
      pageable = PageRequest.of(page, size);
    }

    Instant fromDateInstant = startOfDayInstant(fromDate, 0);
    Instant toDateInstant = startOfDayInstant(toDate, 1);

    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();

    Page<ChangeNote> changeNotesPage;
    List<ChangeNoteDTO> dtos;
    if (accessScope.isAdmin()) {
      changeNotesPage = changeNoteRepository.findByArchivedFalseAndMatchingFilterParameters(filterOptions, fromDateInstant, toDateInstant, pageable);
      dtos = changeNotesPage.getContent().stream()
          .map(changeNote -> ChangeNoteMapper.toDTO(changeNote, accessScope))
          .toList();
    } else {
      filterOptions = new ChangeNoteFilterOptionsDTO(
          filterOptions.query(),
          true,
          filterOptions.hasReleaseNote(),
          filterOptions.includeUnassignedProduct(),
          filterOptions.includeUnassignedScope(),
          filterOptions.includeUnassignedFeature(),
          filterOptions.includeUnassignedCustomer(),
          filterOptions.gitRepositoryIds(),
          filterOptions.filteredIds(),
          filterOptions.customerIds(),
          filterOptions.featureIds(),
          filterOptions.scopeIds(),
          filterOptions.productIds(),
          fromDate,
          toDate
      );

      changeNotesPage = changeNoteRepository.findForCustomerNamesMatchingFilterParameters(accessScope.getCustomerGroups(), filterOptions, fromDateInstant, toDateInstant, pageable);
      dtos = changeNotesPage.getContent().stream()
          .map(note -> {
            note.setDeveloperNotes(null);
            note.setUpgradeNotes(null);
            return note;
          })
          .map(changeNote -> ChangeNoteMapper.toDTO(changeNote, accessScope))
          .toList();
    }

    return new PaginatedResponseDTO<>(dtos, changeNotesPage.getTotalElements());
  }

  /**
   * Retrieves a specific change note by its ID. Takes in to account the access scope of the current user to determine if the change note is viewable.
   * 
   * @param id the ID of the change note to retrieve
   * @return ChangeNoteDTO representing the change note
   * @throws ChangeNoteNotFoundException if no change note with the given ID
   *                                     exists and is accessible to the current user
   */
  public ChangeNoteDTO getChangeNoteById(long id) {
    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();
    
    boolean isAdmin = accessScope.isAdmin();
    if (!isAdmin) {
      ChangeNote changeNote = changeNoteRepository.findForCustomerByIdAndArchivedFalse(id, accessScope.getCustomerGroups()).orElseThrow(() -> new ChangeNoteNotFoundException(id));

      if (!changeNote.isPublished()) {
        throw new ChangeNoteNotFoundException(id);
      }
      return ChangeNoteMapper.toDTO(changeNote, accessScope);
    } else {
      ChangeNote changeNote = changeNoteRepository.findByIdAndArchivedFalse(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));
      return ChangeNoteMapper.toDTO(changeNote, accessScope);
    }
  }

  /**
   * Updates an existing change note with new details from the provided DTO.
   * 
   * @param id                  the ID of the change note to update
   * @param createChangeNoteDTO the DTO containing updated details for the change
   *                            note
   * @return ChangeNoteDTO representing the updated change note
   * @throws ChangeNoteNotFoundException if no change note with the given ID
   *                                     exists
   */
  public ChangeNoteDTO updateChangeNote(long id, CreateChangeNoteDTO createChangeNoteDTO) {
    ChangeNote changeNote = changeNoteRepository.findById(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));

    changeNote.setTitle(createChangeNoteDTO.title());
    changeNote.setReference(createChangeNoteDTO.reference());
    changeNote.setDescription(createChangeNoteDTO.description());
    changeNote.setDeveloperNotes(createChangeNoteDTO.developerNotes());
    changeNote.setUpgradeNotes(createChangeNoteDTO.upgradeNotes());

    if (createChangeNoteDTO.productId() != null) {
      changeNote.setProduct(productRepository.findById(createChangeNoteDTO.productId())
          .orElseThrow(() -> new ProductNotFoundException(createChangeNoteDTO.productId())));
    } else {
      changeNote.setProduct(null);
    }

    if (createChangeNoteDTO.scopeId() != null) {
      changeNote.setScope(scopeRepository.findById(createChangeNoteDTO.scopeId())
          .orElseThrow(() -> new ScopeNotFoundException(createChangeNoteDTO.scopeId())));
    } else {
      changeNote.setScope(null);
    }

    if (createChangeNoteDTO.featureId() != null) {
      changeNote.setFeature(featureRepository.findById(createChangeNoteDTO.featureId())
          .orElseThrow(() -> new FeatureNotFoundException(createChangeNoteDTO.featureId())));
    } else {
      changeNote.setFeature(null);
    }

    if (createChangeNoteDTO.customerId() != null) {
      changeNote.setCustomer(customerRepository.findById(createChangeNoteDTO.customerId())
          .orElseThrow(() -> new CustomerNotFoundException(createChangeNoteDTO.customerId())));
    } else {
      changeNote.setCustomer(null);
    }

    if (createChangeNoteDTO.viewableByEveryone() != null) {
      changeNote.setViewableByEveryone(createChangeNoteDTO.viewableByEveryone());
    }

    return ChangeNoteMapper.toDTO(changeNoteRepository.save(changeNote), AccessScopeFactory.fromCurrentUser());
  }

  /**
   * Alters the published status of a change note by ID to the provided state.
   * 
   * @param id the ID of the change note to change the published status of
   * @param publish a boolean indicating whether to publish (true) or set as draft (false)
   * @throws ChangeNoteNotFoundException if no change note with the given ID
   *                                     exists
   */
  public void publishChangeNote(long id, boolean publish) {
    ChangeNote changeNote = changeNoteRepository.findById(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));
    changeNote.setPublished(publish);
    changeNoteRepository.save(changeNote);
  }

  /**
   * Retrieves the git commit hash and the previous git commit hash for a given change note ID.
   *
   * @param changeNoteId the ID of the change note
   * @return the commit hashes
   */
  @Tool(name = "getGitCommitHashAndPreviousGitCommitHash", description = "Retrieves the git commit hash and the previous git commit hash for a given change note ID.")
  public GitCommitHashAndPreviousGitCommitHash getGitCommitHashAndPreviousGitCommitHash(Long changeNoteId) {
    return changeNoteRepository.findCommitHashAndPreviousCommitHash(changeNoteId);
  }

  /**
   * Checks if a list of change notes all have associated git commit hashes.
   *
   * @param changeNoteIds the IDs of the change notes
   * @return true if the change notes have associated git commit hashes, false otherwise
   */
  public boolean hasCommitHash(List<Long> changeNoteIds) {
    return changeNoteRepository.hasCommitHashAndPreviousCommitHash(changeNoteIds);
  }

  /**
   * Returns the start-of-day instant for {@code localDate} (in the business zone),
   * after adding {@code plusDays}.
   *
   * @param localDate the calendar date to convert, or {@code null}
   * @param plusDays  the number of days to add before taking the start of day
   * @return the corresponding {@link Instant}, or {@code null} if {@code localDate}
   *         is {@code null}
   */
  private Instant startOfDayInstant(LocalDate localDate, int plusDays) {
    if (localDate == null) {
      return null;
    }
    return localDate.plusDays(plusDays).atStartOfDay(BUSINESS_ZONE).toInstant();
  }

}

