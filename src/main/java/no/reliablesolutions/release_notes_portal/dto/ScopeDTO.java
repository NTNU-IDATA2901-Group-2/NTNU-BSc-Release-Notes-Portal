package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Scope;

/**
 * A data transfer object for representing a scope.
 */
public record ScopeDTO(
    long id,
    String name) {
  /**
   * Creates a ScopeDTO from a Scope entity.
   *
   * @param scope the Scope entity
   * @return the ScopeDTO
   */
  public static ScopeDTO fromScope(Scope scope) {
    return new ScopeDTO(scope.getId(), scope.getName());
  }
}
