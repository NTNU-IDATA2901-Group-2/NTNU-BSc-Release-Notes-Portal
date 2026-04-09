package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.Customer;
import no.reliablesolutions.release_notes_portal.domain.repository.CustomerRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.CustomerDTO;
import no.reliablesolutions.release_notes_portal.exception.CustomerNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;

@Service
@AllArgsConstructor
public class CustomerService {
  private final CustomerRepository customerRepository;

  /**
   * Creates a new customer based on the provided DTO.
   *
   * @param customerDTO the DTO containing details for the new customer
   * @return the ID of the created customer
   */
  public long createCustomer(CreateTagDTO customerDTO) {
    Customer customer = new Customer();
    customer.setName(customerDTO.name());
    try {
      return customerRepository.save(customer).getId();
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to create customer");
    }
  }

  /**
   * Retrieves all customers from the repository.
   *
   * @return a list of all customers
   */
  public List<CustomerDTO> getAllCustomers() {
    return customerRepository.findAll().stream()
        .map(CustomerDTO::fromCustomer)
        .toList();
  }

  /**
   * Retrieves a specific customer by its ID.
   *
   * @param id the ID of the customer to retrieve
   * @return a DTO representing the customer
   * @throws CustomerNotFoundException if no customer with the given ID exists
   */
  public CustomerDTO getCustomerById(long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id));
    return CustomerDTO.fromCustomer(customer);
  }

  /**
   * Updates an existing customer with new details from the provided DTO.
   *
   * @param id the ID of the customer to update
   * @param customerDTO the DTO containing updated details for the customer
   * @return a DTO representing the updated customer
   * @throws CustomerNotFoundException if no customer with the given ID exists
   */
  public CustomerDTO updateCustomer(long id, CreateTagDTO customerDTO) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id));
    customer.setName(customerDTO.name());
    try {
      return CustomerDTO.fromCustomer(customerRepository.save(customer));
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to update customer with ID " + id);
    }
  }

  /**
   * Deletes an existing customer by its ID.
   *
   * @param id the ID of the customer to delete
   * @throws CustomerNotFoundException if no customer with the given ID exists
   */
  public void deleteCustomer(long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id));
    customerRepository.delete(customer);
  }

  public List<Customer> getCustomerByName(String customer) {
    return customerRepository.findAllByNameIgnoreCase(customer);
  }
}
