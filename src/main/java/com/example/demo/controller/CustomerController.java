package com.example.demo.controller;

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

import com.example.demo.dto.CustomerDTO;
import com.example.demo.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Customers", description = "Endpoints for managing customers")
@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
public class CustomerController {

	private final CustomerService customerService;
	private final Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@Operation(summary = "Create customer", description = "Creates a new customer with provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Customer created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request payload"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PostMapping("")
	public ResponseEntity<String> createCustomer(@RequestBody CustomerDTO customerDTO) {
		long id = customerService.createCustomer(customerDTO);
		logger.info("Customer created with id: {}", id);
		return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
	}

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

	@Operation(summary = "Update customer", description = "Updates an existing customer with new details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Customer updated successfully"),
		@ApiResponse(responseCode = "404", description = "Customer not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PutMapping("/{id}")
	public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable long id, @RequestBody CustomerDTO customerDTO) {
		CustomerDTO customer = customerService.updateCustomer(id, customerDTO);
		logger.info("Updated customer with id: {}", id);
		return ResponseEntity.ok(customer);
	}

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
