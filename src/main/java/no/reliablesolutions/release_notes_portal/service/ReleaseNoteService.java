package no.reliablesolutions.release_notes_portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.domain.repository.ChangeNoteRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.ReleaseNoteRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ReleaseNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.util.AccessScope;
import no.reliablesolutions.release_notes_portal.util.AccessScopeFactory;
import no.reliablesolutions.release_notes_portal.util.AuthenticationUtil;
import no.reliablesolutions.release_notes_portal.util.ReleaseNoteMapper;

/**
 * Service class for managing release notes. Provides methods for creating, updating, retrieving, and archiving release notes.
 */
@Service
@AllArgsConstructor
public class ReleaseNoteService {

  private final ReleaseNoteRepository releaseNoteRepository;
  private final ChangeNoteRepository changeNoteRepository;

  /**
   * Creates a new release note based on the provided DTO.
   *
   * @param createReleaseNoteDTO the DTO containing details for the new release note
   * @return the ID of the created release note
   * @throws ChangeNoteNotFoundException if any of the specified change note IDs do not
   * correspond to existing change notes
   */
  public long createReleaseNote(CreateReleaseNoteDTO createReleaseNoteDTO) {
    ReleaseNote releaseNote = new ReleaseNote();
    releaseNote.setTag(createReleaseNoteDTO.tag());
    releaseNote.setSummary(createReleaseNoteDTO.summary());
    releaseNote.setPublished(createReleaseNoteDTO.published() != null && createReleaseNoteDTO.published());

    List<ChangeNote> changeNotesInReleaseNote = new ArrayList<>();
    for (Long changeNoteId : createReleaseNoteDTO.changeNoteIds()) {
      ChangeNote changeNote = changeNoteRepository.findById(changeNoteId)
          .orElseThrow(() -> new ChangeNoteNotFoundException(changeNoteId));
      changeNotesInReleaseNote.add(changeNote);
      }
      
    releaseNote.setChangeNotes(changeNotesInReleaseNote);
    releaseNote = releaseNoteRepository.save(releaseNote);

    return releaseNote.getId();
  }

  /**
   * Archives an existing release note by its ID.
   *
   * @param id the ID of the release note to be archived
   * @throws ReleaseNoteNotFoundException if the specified ID does not correspond to an existing
   * release note
   */
  public void archiveReleaseNote(long id) {
    ReleaseNote releaseNote = releaseNoteRepository.findById(id)
        .orElseThrow(() -> new ReleaseNoteNotFoundException(id));
    releaseNote.setArchived(true);
    releaseNoteRepository.save(releaseNote);
  }

  /**
   * Retrieves a list of all non-archived release notes with optional filters for query, published status, and product.
   *
   * @param query optional filter for release note tag or summary containing the query string (case-insensitive)
   * @param published optional filter for release note published status
   * @param productIds optional filter for release note associated product IDs
   *
   * @return a list of ReleaseNoteDTOs representing all non-archived release notes that match the provided filters
   */
  public List<ReleaseNoteDTO> getAllReleaseNotes(String query, Boolean published, List<Long> productIds) {
    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();
    if (accessScope.isAdmin()) {
      return releaseNoteRepository.findByArchivedFalseAndMatchingFilterParameters(query, published, productIds).stream().map(rn -> ReleaseNoteMapper.toDTO(rn, accessScope)).toList();

    } else {
      List<String> customerGroups = AuthenticationUtil.getCustomerGroups();
      return releaseNoteRepository.findByArchivedFalseAndMatchingFilterParametersForCustomers(query, true, productIds, customerGroups).stream().map(releaseNote -> {
        releaseNote.getChangeNotes().forEach(changeNote -> {
          changeNote.setDeveloperNotes(null);
          changeNote.setUpgradeNotes(null);
        });

        return ReleaseNoteMapper.toDTO(releaseNote, accessScope);
      }).toList();
    }
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

    List<String> customerGroups = accessScope.getCustomerGroups();
    boolean isAdmin = AuthenticationUtil.isAdmin();
    if (!isAdmin) {
      LoggerFactory.getLogger(ReleaseNoteService.class).warn("Filtering release note with id {} for customer groups: {}", id, customerGroups);
      releaseNoteOptional.get().getChangeNotes().removeIf(changeNote -> !changeNote.isPublished());


      releaseNoteOptional.get().getChangeNotes().removeIf(changeNote -> {
        if (changeNote.getCustomer() != null) {
          return !customerGroups.contains(changeNote.getCustomer().getName().toUpperCase());
        }
        return false;
      });  

      releaseNoteOptional.get().getChangeNotes().forEach(changeNote -> {
        changeNote.setDeveloperNotes(null);
        changeNote.setUpgradeNotes(null);
      });
      if (releaseNoteOptional.get().getPublished().equals(false)) {
        throw new ReleaseNoteNotFoundException(id);
      }
    }


    return ReleaseNoteMapper.toDTO(releaseNoteOptional.get(), accessScope);
  }

  /**
   * Updates an existing release note with new details from the provided DTO.
   *
   * @param id the ID of the release note to be updated
   * @param createReleaseNoteDTO the DTO containing updated details for the release note
   * @return a ReleaseNoteDTO representing the updated release note
   */
  public ReleaseNoteDTO updateReleaseNote(long id, CreateReleaseNoteDTO createReleaseNoteDTO) {
    Optional<ReleaseNote> releaseNoteOptional = releaseNoteRepository.findById(id);

    if (releaseNoteOptional.isEmpty() || Boolean.TRUE.equals(releaseNoteOptional.get().getArchived())) {
      throw new ReleaseNoteNotFoundException(id);
    }

    ReleaseNote releaseNote = releaseNoteOptional.get();

    for (ChangeNote changeNote : releaseNote.getChangeNotes()) {
      changeNote.setReleaseNotes(null);
      changeNoteRepository.save(changeNote);
    }

    List<ChangeNote> changeNotesInReleaseNote = new ArrayList<>();
    if (createReleaseNoteDTO.changeNoteIds() == null) {
      releaseNote.setChangeNotes(new ArrayList<>());
    } else {
      for (Long changeNoteId : createReleaseNoteDTO.changeNoteIds()) {
        ChangeNote changeNote = changeNoteRepository.findById(changeNoteId)
            .orElseThrow(() -> new ChangeNoteNotFoundException(changeNoteId));
        changeNoteRepository.save(changeNote);
        changeNotesInReleaseNote.add(changeNote);
      }
    }

    releaseNote.setChangeNotes(changeNotesInReleaseNote);
    releaseNote.setTag(createReleaseNoteDTO.tag());
    releaseNote.setSummary(createReleaseNoteDTO.summary());
    releaseNote.setPublished(createReleaseNoteDTO.published());

    releaseNoteRepository.save(releaseNote);
    return ReleaseNoteMapper.toDTO(releaseNote, AccessScopeFactory.fromCurrentUser());

  }
  
  /**
   * Publishes an existing release note by its ID. Privates release note if publish is false.
   *
   * @param id the ID of the release note to be published
   * @param publish a boolean indicating whether to publish (true) or private (false) the release note
   * @throws ReleaseNoteNotFoundException if the specified ID does not correspond to an existing
   * release note
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
}
