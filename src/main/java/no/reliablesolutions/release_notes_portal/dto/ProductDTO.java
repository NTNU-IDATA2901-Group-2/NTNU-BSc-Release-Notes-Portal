package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Product;


/**
 * A data transfer object for representing a product.
 */
public record ProductDTO(
  long id,
  String name
) {
  /**
   * Creates a ProductDTO from a Product entity.
   *
   * @param product the Product entity
   * @return the ProductDTO
   */
  public static ProductDTO fromProduct(Product product) {
    return new ProductDTO(product.getId(), product.getName());
  }
}
