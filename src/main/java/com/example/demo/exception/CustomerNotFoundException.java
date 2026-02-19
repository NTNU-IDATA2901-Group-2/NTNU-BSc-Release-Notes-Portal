package com.example.demo.exception;

import lombok.Getter;

@Getter
public class CustomerNotFoundException extends RuntimeException {
  final Long customerId;

  public CustomerNotFoundException(Long customerId) {
    super("Customer with ID " + customerId + " not found");
    this.customerId = customerId;
  }
}
