package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Scope;

public interface ScopeRepository extends JpaRepository<Scope, Long> {
  
  public List<Scope> findAllByNameIgnoreCase(String name);
}
