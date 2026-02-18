package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.Product;
import com.example.demo.domain.repository.ProductRepository;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  /**
   * Creates a new product based on the provided DTO.
   *
   * @param productDTO the DTO containing details for the new product
   * @return the ID of the created product
   */
  public long createProduct(ProductDTO productDTO) {
    Product product = new Product();
    product.setName(productDTO.name());
    return productRepository.save(product).getId();
  }

  /**
   * Retrieves all products from the repository.
   *
   * @return a list of all products
   */
  public List<ProductDTO> getAllProducts() {
    return productRepository.findAll().stream()
        .map(ProductDTO::fromProduct)
        .toList();
  }

  /**
   * Retrieves a specific product by its ID.
   *
   * @param id the ID of the product to retrieve
   * @return a DTO representing the product
   * @throws ProductNotFoundException if no product with the given ID exists
   */
  public ProductDTO getProductById(long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
    return ProductDTO.fromProduct(product);
  }

  /**
   * Updates an existing product with new details from the provided DTO.
   *
   * @param id the ID of the product to update
   * @param productDTO the DTO containing updated details for the product
   * @return a DTO representing the updated product
   * @throws ProductNotFoundException if no product with the given ID exists
   */
  public ProductDTO updateProduct(long id, ProductDTO productDTO) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
    product.setName(productDTO.name());
    return ProductDTO.fromProduct(productRepository.save(product));
  }

  /**
   * Deletes an existing product by its ID.
   *
   * @param id the ID of the product to delete
   * @throws ProductNotFoundException if no product with the given ID exists
   */
  public void deleteProduct(long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
    productRepository.delete(product);
  }
}
