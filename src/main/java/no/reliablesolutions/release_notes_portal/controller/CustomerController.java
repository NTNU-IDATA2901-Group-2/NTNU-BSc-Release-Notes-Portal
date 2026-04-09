package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.CustomerDTO;
import no.reliablesolutions.release_notes_portal.service.CustomerService;

/**
 * Controller for managing customers.
 */
@Tag(name = "Customers", description = "Endpoints for managing customers")
@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
public class CustomerController {

  private final CustomerService customerService;
  private final Logger logger = LoggerFactory.getLogger(CustomerController.class);

  /**
   * Endpoint for creating a new customer with the provided details.
   * 
   * @param customerDTO the details of the customer to be created
   * @return a ResponseEntity containing the ID of the created customer
   */
  @Operation(summary = "Create customer", description = "Creates a new customer with provided details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Customer created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  public ResponseEntity<String> createCustomer(@Valid @RequestBody CreateTagDTO customerDTO) {
    long id = customerService.createCustomer(customerDTO);
    logger.info("Customer created with id: {}", id);
    return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
  }

  /**
   * Endpoint for retrieving a list of all customers.
   * 
   * @return a ResponseEntity containing a list of CustomerDTOs representing all
   *         customers
   */
  @Operation(summary = "Get all customers", description = "Retrieves a list of all customers")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Customers retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("")
  public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
    List<CustomerDTO> customers = customerService.getAllCustomers();
    logger.info("Retrieved {} customers", customers.size());
    return ResponseEntity.ok(customers);
  }

  /**
   * Endpoint for retrieving details of a specific customer by its ID.
   * 
   * @param id the ID of the customer to retrieve
   * @return a ResponseEntity containing the CustomerDTO representing the
   *         retrieved customer
   */
  @Operation(summary = "Get customer by ID", description = "Retrieves details of a specific customer by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Customer not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable long id) {
    CustomerDTO customer = customerService.getCustomerById(id);
    logger.info("Retrieved customer with id: {}", id);
    return ResponseEntity.ok(customer);
  }

  /**
   * Endpoint for updating an existing customer with new details.
   * 
   * @param id          the ID of the customer to update
   * @param customerDTO the new details for the customer
   * @return a ResponseEntity containing the updated CustomerDTO representing the
   *         updated customer
   */
  @Operation(summary = "Update customer", description = "Updates an existing customer with new details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
      @ApiResponse(responseCode = "404", description = "Customer not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable long id,
      @Valid @RequestBody CreateTagDTO customerDTO) {
    CustomerDTO customer = customerService.updateCustomer(id, customerDTO);
    logger.info("Updated customer with id: {}", id);
    return ResponseEntity.ok(customer);
  }

  /**
   * Endpoint for deleting an existing customer by its ID.
   * 
   * @param id the ID of the customer to delete
   * @return a ResponseEntity indicating the success of the delete operation
   */
  @Operation(summary = "Delete customer", description = "Deletes an existing customer by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Customer not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteCustomer(@PathVariable long id) {
    customerService.deleteCustomer(id);
    logger.info("Deleted customer with id: {}", id);
    return ResponseEntity.ok("Customer deleted successfully");
  }
}
