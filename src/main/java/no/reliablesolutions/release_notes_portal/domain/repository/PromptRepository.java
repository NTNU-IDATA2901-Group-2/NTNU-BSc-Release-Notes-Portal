package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
  /**
   * Finds all prompts ordered by name in ascending order.
   *
   * @return a list of all prompts ordered by name in ascending order
   */
  List<Prompt> findAllByOrderByNameAsc();

  /**
   * Finds a prompt by its name.
   *
   * @param name the name of the prompt to find
   * @return the prompt with the specified name, or null if no such prompt exists
   */
  Prompt findByName(String name);
}
