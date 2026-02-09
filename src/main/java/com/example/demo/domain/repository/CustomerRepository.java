package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
  
}
