package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Finds all products with the specified name, ignoring case.
   *
   * @param product the name of the product to find
   * @return a list of products with the specified name (case-insensitive)
   */
  List<Product> findAllByNameIgnoreCase(String product);
}
