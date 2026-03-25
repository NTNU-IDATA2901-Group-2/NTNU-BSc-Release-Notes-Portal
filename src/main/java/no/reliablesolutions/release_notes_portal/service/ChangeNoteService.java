package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

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
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.CustomerNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.FeatureNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ProductNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ScopeNotFoundException;
import no.reliablesolutions.release_notes_portal.util.AccessScope;
import no.reliablesolutions.release_notes_portal.util.AccessScopeFactory;
import no.reliablesolutions.release_notes_portal.util.AuthenticationUtil;
import no.reliablesolutions.release_notes_portal.util.ChangeNoteMapper;

import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;

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
  public long createChangeNoteFromDto(CreateChangeNoteDTO changeNoteDTO) {
    ChangeNote changeNote = new ChangeNote();

    if (changeNoteDTO != null) {
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

  public void updateChangeNote(ChangeNote changeNote) {
    changeNoteRepository.save(changeNote);
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
   * Retrieves all change notes from the repository, with optional filtering based
   * on query, published status, customer ID, feature ID, scope ID, and product
   * ID.
   * 
   * @param query          optional filter for searching change notes by reference, description, developer notes or upgrade notes
   * @param published      optional filter for published status
   * @param hasReleaseNote optional filter for change notes that have an
   *                       associated release note
   * @param customerIds    optional filter for customer ID
   * @param featureIds     optional filter for feature ID
   * @param scopeIds       optional filter for scope ID
   * @param productIds     optional filter for product ID
   * 
   * @return a list of all change notes that match the provided filters, mapped to
   *         ChangeNoteDTOs
   */
  public List<ChangeNoteDTO> getAllChangeNotes(ChangeNoteFilterOptionsDTO filterOptions) {

    if (filterOptions == null) {
      filterOptions = new ChangeNoteFilterOptionsDTO(null, null, null, null, null, null, null, null);
    }

    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();
    
    if (accessScope.isAdmin()) {
      return changeNoteRepository.findByArchivedFalseAndMatchingFilterParameters(filterOptions).stream()
          .map(changeNote -> ChangeNoteMapper.toDTO(changeNote, accessScope))
          .toList();

    } else {
      filterOptions = new ChangeNoteFilterOptionsDTO(
          filterOptions.query(),
          true,
          filterOptions.hasReleaseNote(),
          filterOptions.filteredIds(),
          filterOptions.customerIds(),
          filterOptions.featureIds(),
          filterOptions.scopeIds(),
          filterOptions.productIds()
      );
      
      return changeNoteRepository.findForCustomerNamesMatchingFilterParameters(accessScope.getCustomerGroups(), filterOptions).stream()
          .map(note -> {
            note.setDeveloperNotes(null);
            note.setUpgradeNotes(null);
            return note;
          })
          .map(changeNote -> ChangeNoteMapper.toDTO(changeNote, accessScope))
          .toList();
    }
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
    AccessScope accessScope = AccessScopeFactory.fromCurrentUser();
    
    boolean isAdmin = AuthenticationUtil.isAdmin();
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
   * @return a DTO representing the updated change note
   * @throws ChangeNoteNotFoundException if no change note with the given ID
   *                                     exists
   */
  public ChangeNoteDTO updateChangeNote(long id, CreateChangeNoteDTO createChangeNoteDTO) {
    ChangeNote changeNote = changeNoteRepository.findById(id).orElseThrow(() -> new ChangeNoteNotFoundException(id));

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

    return ChangeNoteMapper.toDTO(changeNoteRepository.save(changeNote), AccessScopeFactory.fromCurrentUser());
  }

  /**
   * Publishes a change note by setting its published status to true.
   * 
   * @param id the ID of the change note to publish
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
   * @param changeNoteId the ID of the change note
   * @return the commit hashes
   */
  public GitCommitHashAndPreviousGitCommitHash getGitCommitHashAndPreviousGitCommitHash(Long changeNoteId) {
    return changeNoteRepository.findCommitHashAndPreviousCommitHash(changeNoteId);
  }

  /**
   * Checks if a change note has associated git commit hashes.
   * @param changeNoteId the ID of the change note
   * @return true if the change note has associated git commit hashes, false otherwise
   */
  public boolean hasCommitHash(Long changeNoteId) {
    return changeNoteRepository.findCommitHashAndPreviousCommitHash(changeNoteId) != null;
  }
}

