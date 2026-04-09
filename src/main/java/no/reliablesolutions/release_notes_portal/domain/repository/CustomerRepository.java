package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.Optional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  /**
   * Finds a customer by their name, ignoring case.
   *
   * @param customer the name of the customer to find
   * @return a list of customers with the specified name (case-insensitive)
   */
  List<Customer> findAllByNameIgnoreCase(String customer);

  /**
   * Finds a customer by their name.
   * 
   * @param name the name of the customer to find
   * @return an Optional containing the customer with the specified name, or an
   *         empty Optional if no such customer exists
   */
  Optional<Customer> findByName(String name);
}
