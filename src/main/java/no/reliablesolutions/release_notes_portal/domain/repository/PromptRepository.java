package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    List<Prompt> findAllByOrderByNameAsc();
    Prompt findByName(String name);
}
