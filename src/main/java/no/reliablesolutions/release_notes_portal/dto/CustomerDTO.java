package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Customer;

public record CustomerDTO(
  long id,
  String name
) {
  public static CustomerDTO fromCustomer(Customer customer) {
    return new CustomerDTO(customer.getId(), customer.getName());
  }
}
