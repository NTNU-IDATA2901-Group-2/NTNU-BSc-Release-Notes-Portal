package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a scope with the specified ID is not found.
 */
@Getter
public class ScopeNotFoundException extends RuntimeException {
  final Long scopeId;

  /**
   * Constructs a new ScopeNotFoundException with the specified scope ID.
   *
   * @param scopeId the ID of the scope that was not found
   */
  public ScopeNotFoundException(Long scopeId) {
    super("Scope with ID " + scopeId + " not found");
    this.scopeId = scopeId;
  }
}
