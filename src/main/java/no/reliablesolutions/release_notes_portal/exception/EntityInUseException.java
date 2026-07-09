package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when an entity cannot be deleted because it is still referenced by other data.
 */
@Getter
public class EntityInUseException extends RuntimeException {
  final String entityName;
  final Long entityId;

  /**
   * Constructs a new EntityInUseException for the specified entity.
   *
   * @param entityName the display name of the entity type (e.g. "Product")
   * @param entityId the ID of the entity that could not be deleted
   * @param cause the underlying integrity violation
   */
  public EntityInUseException(String entityName, Long entityId, Throwable cause) {
    super(entityName + " with ID " + entityId + " is referenced by other data and cannot be deleted", cause);
    this.entityName = entityName;
    this.entityId = entityId;
  }
}
