package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.Scope;
import com.example.demo.domain.repository.ScopeRepository;
import com.example.demo.dto.ScopeDTO;
import com.example.demo.exception.ScopeNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScopeService {
  private final ScopeRepository scopeRepository;

  /**
   * Creates a new scope based on the provided DTO.
   *
   * @param scopeDTO the DTO containing details for the new scope
   * @return the ID of the created scope
   */
  public long createScope(ScopeDTO scopeDTO) {
    Scope scope = new Scope();
    scope.setName(scopeDTO.name());
    return scopeRepository.save(scope).getId();
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

  /**
   * Updates an existing scope with new details from the provided DTO.
   *
   * @param id the ID of the scope to update
   * @param scopeDTO the DTO containing updated details for the scope
   * @return a DTO representing the updated scope
   * @throws ScopeNotFoundException if no scope with the given ID exists
   */
  public ScopeDTO updateScope(long id, ScopeDTO scopeDTO) {
    Scope scope = scopeRepository.findById(id)
        .orElseThrow(() -> new ScopeNotFoundException(id));
    scope.setName(scopeDTO.name());
    return ScopeDTO.fromScope(scopeRepository.save(scope));
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
