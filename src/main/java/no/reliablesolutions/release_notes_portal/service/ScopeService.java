package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.Scope;
import no.reliablesolutions.release_notes_portal.domain.repository.ScopeRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.ScopeDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.ScopeNotFoundException;

/**
 * Service for managing scope-related operations.
 */
@Service
@AllArgsConstructor
public class ScopeService {
  private final ScopeRepository scopeRepository;
  /**
   * Creates a new scope based on the provided DTO.
   *
   * @param scopeDTO the DTO containing details for the new scope
   * @return the ID of the created scope
   * @throws FailedToSaveEntityException if there was an error saving the scope to the repository
   */
  public long createScope(CreateTagDTO scopeDTO) {
    Scope scope = new Scope();
    scope.setName(scopeDTO.name());
    try {
      return scopeRepository.save(scope).getId();
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to create scope");
    }
  }

  /**
   * Retrieves all scopes from the repository.
   *
   * @return a list of all scopes
   */
  public List<ScopeDTO> getAllScopes() {
    return scopeRepository.findAll().stream()
        .map(ScopeDTO::fromScope)
        .toList();
  }

  /**
   * Retrieves a specific scope by its ID.
   *
   * @param id the ID of the scope to retrieve
   * @return a DTO representing the scope
   * @throws ScopeNotFoundException if no scope with the given ID exists
   */
  public ScopeDTO getScopeById(long id) {
    Scope scope = scopeRepository.findById(id)
        .orElseThrow(() -> new ScopeNotFoundException(id));
    return ScopeDTO.fromScope(scope);
  }

  public List<Scope> getScopeByName(String name) {
    return scopeRepository.findAllByNameIgnoreCase(name);
  }

  /**
   * Updates an existing scope with new details from the provided DTO.
   *
   * @param id the ID of the scope to update
   * @param scopeDTO the DTO containing updated details for the scope
   * @return a DTO representing the updated scope
   * @throws ScopeNotFoundException if no scope with the given ID exists
   * @throws FailedToSaveEntityException if there was an error saving the updated scope to the repository
   */
  public ScopeDTO updateScope(long id, CreateTagDTO scopeDTO) {
    Scope scope = scopeRepository.findById(id)
        .orElseThrow(() -> new ScopeNotFoundException(id));
    scope.setName(scopeDTO.name());

    try {
      scopeRepository.save(scope);
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to update scope with ID " + id);
    }

    return ScopeDTO.fromScope(scope);
  }

  /**
   * Deletes an existing scope by its ID.
   *
   * @param id the ID of the scope to delete
   * @throws ScopeNotFoundException if no scope with the given ID exists
   */
  public void deleteScope(long id) {
    Scope scope = scopeRepository.findById(id)
        .orElseThrow(() -> new ScopeNotFoundException(id));
    scopeRepository.delete(scope);
  }
}
