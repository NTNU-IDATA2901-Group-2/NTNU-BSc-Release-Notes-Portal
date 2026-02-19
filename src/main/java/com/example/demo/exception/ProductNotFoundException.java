package com.example.demo.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
  final Long productId;

  public ProductNotFoundException(Long productId) {
    super("Product with ID " + productId + " not found");
    this.productId = productId;
  }
}
