package no.reliablesolutions.release_notes_portal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;

public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long> {

    
}
