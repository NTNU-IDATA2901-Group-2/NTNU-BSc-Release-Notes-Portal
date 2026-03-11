package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Customer;



public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByName(String name);
}
