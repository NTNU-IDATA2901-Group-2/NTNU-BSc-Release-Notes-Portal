package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
  final Long productId;

  public ProductNotFoundException(Long productId) {
    super("Product with ID " + productId + " not found");
    this.productId = productId;
  }
}
