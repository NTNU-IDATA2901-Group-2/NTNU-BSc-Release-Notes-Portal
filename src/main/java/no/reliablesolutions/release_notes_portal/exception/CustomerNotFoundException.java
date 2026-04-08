package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a customer with the specified ID or name is not found.
 */
@Getter
public class CustomerNotFoundException extends RuntimeException {
  final Long customerId;

  /**
   * Constructs a new CustomerNotFoundException with the specified customer ID.
   * @param customerId the ID of the customer that was not found
   */
  public CustomerNotFoundException(Long customerId) {
    super("Customer with ID " + customerId + " not found");
    this.customerId = customerId;
  }

  /**
   * Constructs a new CustomerNotFoundException with the specified customer name.
   * @param name the name of the customer that was not found
   */
  public CustomerNotFoundException(String name) {
    super("Customer with name " + name + " not found");
    this.customerId = null;
  }
}