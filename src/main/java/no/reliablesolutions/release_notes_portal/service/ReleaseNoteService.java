package no.reliablesolutions.release_notes_portal.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.domain.repository.ChangeNoteRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.ReleaseNoteRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.PaginatedResponseDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteFilterOptionsDTO;
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.InvalidDateRangeException;
import no.reliablesolutions.release_notes_portal.exception.ReleaseNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.util.AccessScope;
import no.reliablesolutions.release_notes_portal.util.AccessScopeFactory;
import no.reliablesolutions.release_notes_portal.util.AuthenticationUtil;
import no.reliablesolutions.release_notes_portal.util.ReleaseNoteMapper;

/**
 * Service class for managing release notes. Provides methods for creating,
 * updating, retrieving, and archiving release notes.
 */
@Service
@AllArgsConstructor
public class ReleaseNoteService {

  private final Logger logger = LoggerFactory.getLogger(ReleaseNoteService.class);
  private final ReleaseNoteRepository releaseNoteRepository;
  private final ChangeNoteRepository changeNoteRepository;

  /** Zone used to resolve a calendar date filter into an absolute instant range. */
  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Oslo");

  /**
   * Creates a new release note based on the provided DTO.
   *
   * @param createReleaseNoteDTO the DTO containing details for the new release
   *                             note
   * @return the ID of the created release note
   * @throws ChangeNoteNotFoundException if any of the specified change note IDs
   *                                     do not
   *                                     correspond to existing change notes
   */
  public long createReleaseNote(CreateReleaseNoteDTO createReleaseNoteDTO) {
    ReleaseNote releaseNote = new ReleaseNote();
    releaseNote.setTag(createReleaseNoteDTO.tag());
    releaseNote.setSummary(createReleaseNoteDTO.summary());
    releaseNote.setPublished(createReleaseNoteDTO.published() != null && createReleaseNoteDTO.published());

    List<ChangeNote> changeNotesInReleaseNote = new ArrayList<>();
    if (createReleaseNoteDTO.changeNoteIds() != null) {
      for (Long changeNoteId : createReleaseNoteDTO.changeNoteIds()) {
        ChangeNote changeNote = changeNoteRepository.findById(changeNoteId)
            .orElseThrow(() -> new ChangeNoteNotFoundException(changeNoteId));
        changeNotesInReleaseNote.add(changeNote);
      }
    }

    releaseNote.setChangeNotes(changeNotesInReleaseNote);
    releaseNote = releaseNoteRepository.save(releaseNote);

    return releaseNote.getId();
  }

  /**
   * Archives an existing release note by its ID.
   *
   * @param id the ID of the release note to be archived
   * @throws ReleaseNoteNotFoundException if the specified ID does not correspond
   *                                      to an existing
   *                                      release note
   */
  public void archiveReleaseNote(long id) {
    ReleaseNote releaseNote = releaseNoteRepository.findById(id)
        .orElseThrow(() -> new ReleaseNoteNotFoundException(id));
    releaseNote.setArchived(true);
    releaseNoteRepository.save(releaseNote);
  }

  public PaginatedResponseDTO<List<ReleaseNoteDTO>> getAllReleaseNotes(ReleaseNoteFilterOptionsDTO filterOptions, Integer page, Integer size) {

    if (filterOptions == null) {
      filterOptions = new ReleaseNoteFilterOptionsDTO(null, null, null, null, null, null);
    }

    LocalDate fromDate = filterOptions.fromDate();
    LocalDate toDate = filterOptions.toDate();

    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new InvalidDateRangeException(fromDate, toDate);
    }

    Pageable pageable;
    if (page == null || size == null) {
      logger.warn("Page number or page size is null. Returning all release notes without pagination.");
      pageable = Pageable.unpaged();
    } else if (page < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    } else if (size <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    } else {
      pageable = PageRequest.of(page, size);
    }

    Instant fromDateInstant = getInstantFromLocalDate(fromDate, false);
    Instant toDateInstant = getInstantFromLocalDate(toDate, true);

    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();

    Page<ReleaseNote> releaseNotesPage;
    if (accessScope.isAdmin()) {
      releaseNotesPage = releaseNoteRepository.findByArchivedFalseAndMatchingFilterParameters(filterOptions, fromDateInstant, toDateInstant, pageable);
    } else {
      filterOptions = new ReleaseNoteFilterOptionsDTO(
          filterOptions.query(),
          true,
          filterOptions.includeUnassignedProduct(),
          filterOptions.productIds(),
          fromDate,
          toDate
      );
      List<String> customerGroups = AuthenticationUtil.getCustomerGroups();
      
      releaseNotesPage = releaseNoteRepository
          .findByArchivedFalseAndMatchingFilterParametersForCustomers(filterOptions, fromDateInstant, toDateInstant, customerGroups, pageable);
    }
    List<ReleaseNoteDTO> dtos = releaseNotesPage.getContent()
      .stream()
      .map(rn -> ReleaseNoteMapper.toDTO(rn, accessScope)).toList();
    
    return new PaginatedResponseDTO<>(dtos, releaseNotesPage.getTotalPages());
  }

  /**
   * Retrieves details of a specific non-archived release note by its ID.
   *
   * @param id the ID of the release note to be retrieved
   * @return a ReleaseNoteDTO representing the release note with the specified ID
   */
  public ReleaseNoteDTO getReleaseNoteById(long id) {
    Optional<ReleaseNote> releaseNoteOptional = releaseNoteRepository.findById(id);
    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();

    if (releaseNoteOptional.isEmpty() || Boolean.TRUE.equals(releaseNoteOptional.get().getArchived())) {
      throw new ReleaseNoteNotFoundException(id);
    }

    ReleaseNote releaseNote = releaseNoteOptional.get();

    boolean isAdmin = AuthenticationUtil.isAdmin();
    if (!isAdmin && releaseNote.getPublished().equals(false)) {
      throw new ReleaseNoteNotFoundException(id);
    }

    return ReleaseNoteMapper.toDTO(releaseNote, accessScope);
  }

  /**
   * Updates an existing release note with new details from the provided DTO.
   *
   * @param id                   the ID of the release note to be updated
   * @param createReleaseNoteDTO the DTO containing updated details for the
   *                             release note
   * @return a ReleaseNoteDTO representing the updated release note
   */
  public ReleaseNoteDTO updateReleaseNote(long id, CreateReleaseNoteDTO createReleaseNoteDTO) {
    Optional<ReleaseNote> releaseNoteOptional = releaseNoteRepository.findById(id);

    if (releaseNoteOptional.isEmpty() || Boolean.TRUE.equals(releaseNoteOptional.get().getArchived())) {
      throw new ReleaseNoteNotFoundException(id);
    }

    ReleaseNote releaseNote = releaseNoteOptional.get();

    List<ChangeNote> changeNotesInReleaseNote = new ArrayList<>();
    if (createReleaseNoteDTO.changeNoteIds() != null) {
      for (Long changeNoteId : createReleaseNoteDTO.changeNoteIds()) {
        ChangeNote changeNote = changeNoteRepository.findById(changeNoteId)
            .orElseThrow(() -> new ChangeNoteNotFoundException(changeNoteId));
        changeNotesInReleaseNote.add(changeNote);
      }
    }

    for (ChangeNote changeNote : releaseNote.getChangeNotes()) {
      changeNote.removeReleaseNote(releaseNote);
      changeNoteRepository.save(changeNote);
    }

    for (ChangeNote changeNote : changeNotesInReleaseNote) {
      changeNote.addReleaseNote(releaseNote);
      changeNoteRepository.save(changeNote);
    }

    releaseNote.setChangeNotes(changeNotesInReleaseNote);
    releaseNote.setTag(createReleaseNoteDTO.tag());
    releaseNote.setSummary(createReleaseNoteDTO.summary());
    releaseNote.setPublished(createReleaseNoteDTO.published());

    releaseNoteRepository.save(releaseNote);
    return ReleaseNoteMapper.toDTO(releaseNote, AccessScopeFactory.fromCurrentUser());
  }

  /**
   * Publishes an existing release note by its ID. Privates release note if
   * publish is false.
   *
   * @param id      the ID of the release note to be published
   * @param publish a boolean indicating whether to publish (true) or private
   *                (false) the release note
   * @throws ReleaseNoteNotFoundException if the specified ID does not correspond
   *                                      to an existing
   *                                      release note
   */
  public void publishReleaseNote(long id, boolean publish) {
    Optional<ReleaseNote> releaseNoteOptional = releaseNoteRepository.findById(id);

    if (releaseNoteOptional.isEmpty() || Boolean.TRUE.equals(releaseNoteOptional.get().getArchived())) {
      throw new ReleaseNoteNotFoundException(id);
    }

    ReleaseNote releaseNote = releaseNoteOptional.get();
    releaseNote.setPublished(publish);
    releaseNoteRepository.save(releaseNote);
  }

  private Instant getInstantFromLocalDate(LocalDate localDate, boolean inclusive) {
    if (localDate == null) {
      return null;
    }
    if (inclusive) {
      return localDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
    } else {
      return localDate.atStartOfDay(BUSINESS_ZONE).toInstant();
    }
  }
}
