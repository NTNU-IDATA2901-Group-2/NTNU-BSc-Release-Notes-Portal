package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeImpact;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeImpact.TestingNeed;

/**
 * A data transfer object for representing the impact a change has on a product.
 */
public record ChangeImpactDTO(
    long id,
    ProductDTO product,
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
        changeImpact.getProduct() != null ? ProductDTO.fromProduct(changeImpact.getProduct()) : null,
        changeImpact.getWhatIsChanged(),
        changeImpact.getWhatShouldBeTested(),
        changeImpact.getTestingNeed());
  }
}
