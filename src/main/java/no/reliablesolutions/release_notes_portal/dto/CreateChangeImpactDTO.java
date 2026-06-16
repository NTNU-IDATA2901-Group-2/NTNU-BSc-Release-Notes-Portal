package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeImpact.TestingNeed;

public record CreateChangeImpactDTO(
    Long featureId,
    String whatIsChanged,
    String whatShouldBeTested,
    TestingNeed testingNeed) {
}
