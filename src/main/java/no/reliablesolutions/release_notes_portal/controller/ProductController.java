package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.ProductDTO;
import no.reliablesolutions.release_notes_portal.service.ProductService;

/**
 * Controller for managing products.
 */
@Tag(name = "Products", description = "Endpoints for managing products")
@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final Logger logger = LoggerFactory.getLogger(ProductController.class);

  /**
   * Endpoint for creating a new product with the provided details.
   * 
   * @param productDTO the details of the product to be created
   * @return a ResponseEntity containing the ID of the created product
   */
  @Operation(summary = "Create product", description = "Creates a new product with provided details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Product created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> createProduct(@Valid @RequestBody CreateTagDTO productDTO) {
    long id = productService.createProduct(productDTO);
    logger.info("Product created with id: {}", id);
    return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
  }

  /**
   * Endpoint for retrieving a list of all products.
   * 
   * @return a ResponseEntity containing a list of all products
   */
  @Operation(summary = "Get all products", description = "Retrieves a list of all products")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("")
  public ResponseEntity<List<ProductDTO>> getAllProducts() {
    List<ProductDTO> products = productService.getAllProducts();
    logger.info("Retrieved {} products", products.size());
    return ResponseEntity.ok(products);
  }

  /**
   * Endpoint for retrieving details of a specific product by its ID.
   * 
   * @param id the ID of the product to be retrieved
   * @return a ResponseEntity containing the details of the requested product
   */
  @Operation(summary = "Get product by ID", description = "Retrieves details of a specific product by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ProductDTO> getProductById(@PathVariable long id) {
    ProductDTO product = productService.getProductById(id);
    logger.info("Retrieved product with id: {}", id);
    return ResponseEntity.ok(product);
  }

  /**
   * Endpoint for updating an existing product with new details.
   * 
   * @param id         the ID of the product to be updated
   * @param productDTO the new details of the product
   * @return a ResponseEntity containing the updated product details
   */
  @Operation(summary = "Update product", description = "Updates an existing product with new details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product updated successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ProductDTO> updateProduct(@PathVariable long id, @Valid @RequestBody CreateTagDTO productDTO) {
    ProductDTO product = productService.updateProduct(id, productDTO);
    logger.info("Updated product with id: {}", id);
    return ResponseEntity.ok(product);
  }

  /**
   * Endpoint for deleting an existing product by its ID.
   * 
   * @param id the ID of the product to be deleted
   * @return a ResponseEntity indicating the success of the delete operation
   */
  @Operation(summary = "Delete product", description = "Deletes an existing product by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> deleteProduct(@PathVariable long id) {
    productService.deleteProduct(id);
    logger.info("Deleted product with id: {}", id);
    return ResponseEntity.ok("Product deleted successfully");
  }
}
