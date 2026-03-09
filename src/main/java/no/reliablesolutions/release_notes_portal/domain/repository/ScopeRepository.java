package no.reliablesolutions.release_notes_portal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Scope;

public interface ScopeRepository extends JpaRepository<Scope, Long> {
  
}
