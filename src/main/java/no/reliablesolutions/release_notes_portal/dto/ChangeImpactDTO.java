package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeImpact;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeImpact.TestingNeed;

/**
 * A data transfer object for representing the impact a change has on a scope.
 */
public record ChangeImpactDTO(
    long id,
    ScopeDTO scope,
    String whatIsChanged,
    String whatShouldBeTested,
    TestingNeed testingNeed) {

  /**
   * Creates a ChangeImpactDTO from a ChangeImpact entity.
   *
   * @param changeImpact the ChangeImpact entity
   * @return the ChangeImpactDTO
   */
  public static ChangeImpactDTO fromEntity(ChangeImpact changeImpact) {
    return new ChangeImpactDTO(
        changeImpact.getId(),
        changeImpact.getScope() != null ? ScopeDTO.fromScope(changeImpact.getScope()) : null,
        changeImpact.getWhatIsChanged(),
        changeImpact.getWhatShouldBeTested(),
        changeImpact.getTestingNeed());
  }
}
