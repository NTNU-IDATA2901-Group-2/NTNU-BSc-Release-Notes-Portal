package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.Customer;
import com.example.demo.domain.repository.CustomerRepository;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.exception.CustomerNotFoundException;

import lombok.AllArgsConstructor;

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
  public long createCustomer(CustomerDTO customerDTO) {
    Customer customer = new Customer();
    customer.setName(customerDTO.name());
    return customerRepository.save(customer).getId();
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
  public CustomerDTO updateCustomer(long id, CustomerDTO customerDTO) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id));
    customer.setName(customerDTO.name());
    return CustomerDTO.fromCustomer(customerRepository.save(customer));
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
}
