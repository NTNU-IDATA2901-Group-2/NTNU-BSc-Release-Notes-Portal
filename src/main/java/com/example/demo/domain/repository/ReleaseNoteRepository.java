package com.example.demo.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.domain.entity.ReleaseNote;

public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Long> {

  @Query("""
      SELECT r
      FROM ReleaseNote r
      WHERE r.archived = false AND
        ((:query IS NULL OR :query = '') OR
        LOWER(r.tag) LIKE LOWER('%' || :query || '%') OR
        LOWER(r.summary) LIKE LOWER('%' || :query || '%'))
      """)
  public List<ReleaseNote> findByArchivedFalseAndContainingQuery(String query);
}
