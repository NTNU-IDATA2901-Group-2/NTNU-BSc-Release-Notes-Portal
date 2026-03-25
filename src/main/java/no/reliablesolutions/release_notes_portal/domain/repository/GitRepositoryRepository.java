package no.reliablesolutions.release_notes_portal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;

public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long> {

  @Query("""
      SELECT g
      FROM GitRepository g
      JOIN ChangeNote c ON c.gitRepository = g
      WHERE c.id = :changeNoteId
      """)
  GitRepository findByChangeNoteId(long changeNoteId);

    
}
