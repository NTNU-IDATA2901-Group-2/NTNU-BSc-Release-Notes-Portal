package com.example.demo.dto;

import com.example.demo.domain.entity.Customer;

public record CustomerDTO(
  long id,
  String name
) {
  public static CustomerDTO fromCustomer(Customer customer) {
    return new CustomerDTO(customer.getId(), customer.getName());
  }
}
