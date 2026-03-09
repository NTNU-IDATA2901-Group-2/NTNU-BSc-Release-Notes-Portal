package no.reliablesolutions.release_notes_portal.dto;

import no.reliablesolutions.release_notes_portal.domain.entity.Product;



public record ProductDTO(
  long id,
  String name
) {
  public static ProductDTO fromProduct(Product product) {
    return new ProductDTO(product.getId(), product.getName());
  }
}
