package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

@Getter
public class CustomerNotFoundException extends RuntimeException {
  final Long customerId;

  public CustomerNotFoundException(Long customerId) {
    super("Customer with ID " + customerId + " not found");
    this.customerId = customerId;
  }
}
