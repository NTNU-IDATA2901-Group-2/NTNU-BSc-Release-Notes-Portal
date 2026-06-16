package no.reliablesolutions.release_notes_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateChangeImpactDTO {
  private Long featureId;
  private String whatIsChanged;
  private String whatShouldBeTested;
  private String testingNeed;
}
