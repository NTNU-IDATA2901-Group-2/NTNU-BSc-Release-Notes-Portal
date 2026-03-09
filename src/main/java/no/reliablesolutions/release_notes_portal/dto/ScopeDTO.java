package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Scope;



public record ScopeDTO(
  long id,
  String name
) {
  public static ScopeDTO fromScope(Scope scope) {
    return new ScopeDTO(scope.getId(), scope.getName());
  }
}
