package no.reliablesolutions.release_notes_portal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;

public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long> {

  /**
   * Finds a Git repository associated with the specified change note ID.
   *
   * @param changeNoteId the ID of the change note
   * @return the Git repository associated with the specified change note ID
   */
  @Query("""
      SELECT g
      FROM GitRepository g
      JOIN ChangeNote c ON c.gitRepository = g
      WHERE c.id = :changeNoteId
      """)
  GitRepository findByChangeNoteId(long changeNoteId);

  /**
   * Clears all references to the Git repository with the specified ID from any associated change notes.
   * @param id the ID of the Git repository for which to clear references
   */
  @Query("""
      UPDATE ChangeNote c
      SET c.gitRepository = null
      WHERE c.gitRepository.id = :id
      """)
  @Transactional
  @Modifying
  void clearGitRepositoryReferencesById(long id);
}
