package no.reliablesolutions.release_notes_portal.dto;

import jakarta.validation.constraints.NotNull;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeImpact.TestingNeed;

public record CreateChangeImpactDTO(
    @NotNull Long featureId,
    @NotNull String whatIsChanged,
    @NotNull String whatShouldBeTested,
    @NotNull TestingNeed testingNeed) {
}
