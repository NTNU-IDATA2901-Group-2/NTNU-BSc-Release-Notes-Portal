package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.Product;
import no.reliablesolutions.release_notes_portal.domain.repository.ProductRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.ProductDTO;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.ProductNotFoundException;

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
  public long createProduct(CreateTagDTO productDTO) {
    Product product = new Product();
    product.setName(productDTO.name());
    try {
      return productRepository.save(product).getId();
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to create product");
    }
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
  public ProductDTO updateProduct(long id, CreateTagDTO productDTO) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
    product.setName(productDTO.name());
    try {
      return ProductDTO.fromProduct(productRepository.save(product));
    } catch (Exception _) {
      throw new FailedToSaveEntityException("Failed to update product with ID " + id);
    }
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

  /**
   * Retrieves a list of products that match the given name, ignoring case.
   *
   * @param product the name of the product to search for
   * @return a list of products that match the given name
   */
  public List<Product> getProductByName(String product) {
    return productRepository.findAllByNameIgnoreCase(product);
  }
}
