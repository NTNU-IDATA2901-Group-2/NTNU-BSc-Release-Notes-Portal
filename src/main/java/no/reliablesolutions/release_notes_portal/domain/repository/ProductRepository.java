package no.reliablesolutions.release_notes_portal.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import no.reliablesolutions.release_notes_portal.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findAllByNameIgnoreCase(String product);
  
}
