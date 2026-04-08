package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a product with the specified ID is not found.
 */
@Getter
public class ProductNotFoundException extends RuntimeException {
  final Long productId;

  /**
   * Constructs a new ProductNotFoundException with the specified product ID.
   *
   * @param productId the ID of the product that was not found
   */
  public ProductNotFoundException(Long productId) {
    super("Product with ID " + productId + " not found");
    this.productId = productId;
  }
}
