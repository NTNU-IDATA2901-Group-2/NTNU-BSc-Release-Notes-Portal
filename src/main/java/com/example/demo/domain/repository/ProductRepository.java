package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
  
}
