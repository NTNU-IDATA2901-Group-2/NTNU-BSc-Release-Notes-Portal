package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.ChangeNote;
import com.example.demo.domain.entity.ReleaseNote;
import com.example.demo.domain.repository.ChangeNoteRepository;
import com.example.demo.domain.repository.ReleaseNoteRepository;
import com.example.demo.dto.CreateReleaseNoteDTO;
import com.example.demo.dto.ReleaseNoteDTO;
import com.example.demo.exception.ChangeNoteNotFoundException;
import com.example.demo.exception.ReleaseNoteNotFoundException;
import com.example.demo.exception.ChangeNoteAlreadyHasReleaseNoteException;

import lombok.AllArgsConstructor;

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
    releaseNote.setPublished(createReleaseNoteDTO.published() != null ? createReleaseNoteDTO.published() : false);

    List<ChangeNote> changeNotesInReleaseNote = new ArrayList<>();
    if (createReleaseNoteDTO.changeNoteIds() != null) {
      for (Long changeNoteId : createReleaseNoteDTO.changeNoteIds()) {
        ChangeNote changeNote = changeNoteRepository.findById(changeNoteId)
            .orElseThrow(() -> new ChangeNoteNotFoundException(changeNoteId));

        if (changeNote.getReleaseNote() != null) {
          throw new ChangeNoteAlreadyHasReleaseNoteException(changeNoteId, changeNote.getReleaseNote().getId());
        }
        releaseNote = releaseNoteRepository.save(releaseNote);
        changeNote.setReleaseNote(releaseNote);
        changeNoteRepository.save(changeNote);
        changeNotesInReleaseNote.add(changeNote);
      }
    }
    
    releaseNote.setChangeNotes(changeNotesInReleaseNote);



    return releaseNoteRepository.save(releaseNote).getId();
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
   * Retrieves a list of all non-archived release notes with optional filters for query and published status.
   *
   * @param query optional filter for release note tag or summary containing the query string (case-insensitive)
   * @param published optional filter for release note published status
   *
   * @return a list of ReleaseNoteDTOs representing all non-archived release notes that match the provided filters
   */
  public List<ReleaseNoteDTO> getAllReleaseNotes(String query, Boolean published) {
    return releaseNoteRepository.findByArchivedFalseAndContainingQuery(query).stream().map(ReleaseNoteDTO::fromReleaseNote).toList();
  }

  /**
   * Retrieves details of a specific non-archived release note by its ID.
   *
   * @param id the ID of the release note to be retrieved
   * @return a ReleaseNoteDTO representing the release note with the specified ID
   */
  public ReleaseNoteDTO getReleaseNoteById(long id) {
    Optional<ReleaseNote> releaseNoteOptional = releaseNoteRepository.findById(id);
    
    if (releaseNoteOptional.isEmpty() || Boolean.TRUE.equals(releaseNoteOptional.get().getArchived())) {
      throw new ReleaseNoteNotFoundException(id);
    }
    return ReleaseNoteDTO.fromReleaseNote(releaseNoteOptional.get());
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
      changeNote.setReleaseNote(null);
      changeNoteRepository.save(changeNote);
    }

    List<ChangeNote> changeNotesInReleaseNote = new ArrayList<>();
    if (createReleaseNoteDTO.changeNoteIds() == null) {
      releaseNote.setChangeNotes(new ArrayList<ChangeNote>());
    } else {
      for (Long changeNoteId : createReleaseNoteDTO.changeNoteIds()) {
        ChangeNote changeNote = changeNoteRepository.findById(changeNoteId)
            .orElseThrow(() -> new ChangeNoteNotFoundException(changeNoteId));

        if (changeNote.getReleaseNote() != null && changeNote.getReleaseNote().getId() != releaseNote.getId()) {
          throw new ChangeNoteAlreadyHasReleaseNoteException(changeNoteId, changeNote.getReleaseNote().getId());
        }
        changeNote.setReleaseNote(releaseNote);
        changeNoteRepository.save(changeNote);
        changeNotesInReleaseNote.add(changeNote);
      }
    }

    releaseNote.setChangeNotes(changeNotesInReleaseNote);
    releaseNote.setTag(createReleaseNoteDTO.tag());
    releaseNote.setSummary(createReleaseNoteDTO.summary());
    releaseNote.setPublished(createReleaseNoteDTO.published());

    releaseNoteRepository.save(releaseNote);
    return ReleaseNoteDTO.fromReleaseNote(releaseNote);

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

    if (releaseNoteOptional.isEmpty() || releaseNoteOptional.get().getArchived()) {
      throw new ReleaseNoteNotFoundException(id);
    }

    ReleaseNote releaseNote = releaseNoteOptional.get();
    releaseNote.setPublished(publish);
    releaseNoteRepository.save(releaseNote);
  }
}
