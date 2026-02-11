package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.ChangeNote;
import com.example.demo.domain.repository.ChangeNoteRepository;
import com.example.demo.domain.repository.CustomerRepository;
import com.example.demo.domain.repository.FeatureRepository;
import com.example.demo.domain.repository.ProductRepository;
import com.example.demo.domain.repository.ScopeRepository;
import com.example.demo.dto.CreateChangeNoteDTO;
import com.example.demo.dto.ChangeNoteDTO;
import com.example.demo.exception.ChangeNoteNotFoundException;
import com.example.demo.exception.CustomerNotFoundException;
import com.example.demo.exception.FeatureNotFoundException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.exception.ScopeNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChangeNoteService {
  private final ChangeNoteRepository changeNoteRepository;
  private final ProductRepository productRepository;
  private final ScopeRepository scopeRepository;
  private final FeatureRepository featureRepository;
  private final CustomerRepository customerRepository;

  /**
   * Creates a new change note based on the provided DTO. Validates and sets all
   * related entities before saving.
   * 
   * @param changeNoteDTO the DTO containing details for the new change note
   * @return the ID of the created change note
   * @throws ProductNotFoundException  if the specified product ID does not exist
   * @throws ScopeNotFoundException    if the specified scope ID does not exist
   * @throws FeatureNotFoundException  if the specified feature ID does not exist
   * @throws CustomerNotFoundException if the specified customer ID does not exist
   */
  public long createChangeNote(CreateChangeNoteDTO changeNoteDTO) {
    ChangeNote changeNote = new ChangeNote();

    changeNote.setReference(changeNoteDTO.reference());
    changeNote.setDescription(changeNoteDTO.description());
    changeNote.setDeveloperNotes(changeNoteDTO.developerNotes());
    changeNote.setUpgradeNotes(changeNoteDTO.upgradeNotes());
    changeNote.setChangeSource(changeNoteDTO.changeSource());

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

    return changeNoteRepository.save(changeNote).getId();
  }

  /**
   * Archives a change note
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
   * Retrieves all change notes from the repository.
   * 
   * @return a list of all change notes
   */
  public List<ChangeNoteDTO> getAllChangeNotes() {
    return changeNoteRepository.findByArchivedFalse().stream()
        .map(ChangeNoteDTO::fromChangeNote)
        .toList();
  }

  /**
   * Retrieves a specific change note by its ID.
   * 
   * @param id the ID of the change note to retrieve
   * @return a DTO representing the change note
   * @throws ChangeNoteNotFoundException if no change note with the given ID
   *                                     exists
   */
  public ChangeNoteDTO getChangeNoteById(long id) {
    ChangeNote changeNote = changeNoteRepository.findById(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));
    return ChangeNoteDTO.fromChangeNote(changeNote);
  }

  /**
   * Updates an existing change note with new details from the provided DTO. 
   * 
   * @param id the ID of the change note to update
   * @param createChangeNoteDTO the DTO containing updated details for the change note
   * @return a DTO representing the updated change note
   * @throws ChangeNoteNotFoundException if no change note with the given ID exists
   */
  public ChangeNoteDTO updateChangeNote(long id, CreateChangeNoteDTO createChangeNoteDTO) {
    ChangeNote changeNote = changeNoteRepository.findById(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));

    changeNote.setReference(createChangeNoteDTO.reference());
    changeNote.setDescription(createChangeNoteDTO.description());
    changeNote.setDeveloperNotes(createChangeNoteDTO.developerNotes());
    changeNote.setUpgradeNotes(createChangeNoteDTO.upgradeNotes());
    changeNote.setChangeSource(createChangeNoteDTO.changeSource());

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

    return ChangeNoteDTO.fromChangeNote(changeNoteRepository.save(changeNote));
  }
}
