package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

@Getter
public class ScopeNotFoundException extends RuntimeException {
  final Long scopeId;

  public ScopeNotFoundException(Long scopeId) {
    super("Scope with ID " + scopeId + " not found");
    this.scopeId = scopeId;
  } 
}
