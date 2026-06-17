package no.reliablesolutions.release_notes_portal.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity representing a release timeline
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReleaseTimeline {
    private LocalDate previewAvailableFrom;
    private LocalDate recommendedTestPhaseFrom;
    private LocalDate recommendedTestPhaseTo;
    private LocalDate plannedProductionDeployment;
}