package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Customer;

/**
 * A data transfer object for representing a customer.
 */
public record CustomerDTO(
    long id,
    String name) {
  /**
   * Creates a CustomerDTO from a Customer entity.
   *
   * @param customer the Customer entity
   * @return the CustomerDTO
   */
  public static CustomerDTO fromCustomer(Customer customer) {
    return new CustomerDTO(customer.getId(), customer.getName());
  }
}
