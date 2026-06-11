package no.reliablesolutions.release_notes_portal.domain.entity;

import java.time.Instant;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity representing a release timeline
 */
@Embeddable
@NoArgsConstructor
@Getter
public class ReleaseTimeline {
    private Instant previewAvailableFrom;
    private Instant recommendedTestPhaseFrom;
    private Instant recommendedTestPhaseTo;
    private Instant plannedProductionDeployment;
}