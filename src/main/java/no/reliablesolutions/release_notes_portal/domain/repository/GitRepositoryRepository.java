package no.reliablesolutions.release_notes_portal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;

/**
 * Repository for managing GitRepository entities.
 */
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
   * Finds a Git repository by its name.
   *
   * @param name the name of the Git repository
   * @return the Git repository with the specified name, or null if none exists
   */
  GitRepository findFirstByName(String name);

}
