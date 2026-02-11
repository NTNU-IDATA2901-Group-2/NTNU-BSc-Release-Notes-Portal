package com.example.demo.dto;

import com.example.demo.domain.entity.Product;

public record ProductDTO(
  long id,
  String name
) {
  public static ProductDTO fromProduct(Product product) {
    return new ProductDTO(product.getId(), product.getName());
  }
}
