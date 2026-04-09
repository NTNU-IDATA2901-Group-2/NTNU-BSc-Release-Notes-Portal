package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Scope;

public interface ScopeRepository extends JpaRepository<Scope, Long> {
  
  /**
   * Finds all scopes with the specified name, ignoring case.
   *
   * @param name the name of the scope to find
   * @return a list of scopes with the specified name (case-insensitive)
   */
  public List<Scope> findAllByNameIgnoreCase(String name);
}
