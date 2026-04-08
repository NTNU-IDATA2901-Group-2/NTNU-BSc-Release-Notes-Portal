package no.reliablesolutions.release_notes_portal.dto;

import java.util.List;

public record ChangeNoteFilterOptionsDTO(
      String query,
      Boolean published,
      Boolean hasReleaseNote,
      List<Long> gitRepositoryIds,
      List<Long> filteredIds,
      List<Long> customerIds,
      List<Long> featureIds,
      List<Long> scopeIds,
      List<Long> productIds
) {}
