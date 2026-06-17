package no.reliablesolutions.release_notes_portal.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseTimeline;

/**
 * DTO class for a release timeline.
 */
@AllArgsConstructor
@Getter
public class ReleaseTimelineDTO {
    private LocalDate previewAvailableFrom;
    private LocalDate recommendedTestPhaseFrom;
    private LocalDate recommendedTestPhaseTo;
    private LocalDate plannedProductionDeployment;
    
    public static ReleaseTimelineDTO fromEntity(ReleaseTimeline timeline) {
      if (timeline == null) {
        return null;
      }
      return new ReleaseTimelineDTO(
        timeline.getPreviewAvailableFrom(),
        timeline.getRecommendedTestPhaseFrom(),
        timeline.getRecommendedTestPhaseTo(),
        timeline.getPlannedProductionDeployment()
      );
    }
}
