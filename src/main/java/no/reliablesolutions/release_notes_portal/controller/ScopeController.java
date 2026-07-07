package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import no.reliablesolutions.release_notes_portal.dto.ScopeDTO;
import no.reliablesolutions.release_notes_portal.service.ScopeService;

/**
 * Controller for managing scope endpoints.
 */
@Tag(name = "Scopes", description = "Endpoints for managing scopes")
@RestController
@AllArgsConstructor
@RequestMapping("/api/scopes")
public class ScopeController {
  private final ScopeService scopeService;
  private final Logger logger = LoggerFactory.getLogger(ScopeController.class);

  /**
   * Retreives a list of all scopes.
   * 
   * @return ResponseEntity containing a list of all scopes
   */
  @Operation(summary = "Get all scopes", description = "Retrieves a list of all scopes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scopes retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("")
  public ResponseEntity<List<ScopeDTO>> getAllScopes() {
    logger.info("Retrieved all scopes");
    return ResponseEntity.ok(scopeService.getAllScopes());
  }

  /**
   * Retrieves details of a specific scope by its ID.
   *
   * @param id the ID of the scope to be retrieved
   * @return a ResponseEntity containing the details of the requested scope
   */
  @Operation(summary = "Get scope by ID", description = "Retrieves details of a specific scope by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scope retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scope not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ScopeDTO> getScopeById(@PathVariable Long id) {
    logger.info("Retrieved scope with id: {}", id);
    return ResponseEntity.ok(scopeService.getScopeById(id));
  }

  /**
   * Creates a new scope with the provided details.
   *
   * @param scopeDetails the details of the scope to be created
   * @return a ResponseEntity containing the ID of the created scope
   */
  @Operation(summary = "Create scope", description = "Creates a new scope with provided details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scope created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Long> createScope(@Valid @RequestBody CreateTagDTO scopeDetails) {
    logger.info("Created scope with details: {}", scopeDetails);
    long createdScope = scopeService.createScope(scopeDetails);
    return ResponseEntity.ok(createdScope);
  }

  /**
   * Updates an existing scope with new details.
   *
   * @param id the ID of the scope to be updated
   * @param scopeDetails the new details of the scope
   * @return a ResponseEntity containing a confirmation message with the updated scope's ID
   */
  @Operation(summary = "Update scope", description = "Updates an existing scope with new details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scope updated successfully"),
      @ApiResponse(responseCode = "404", description = "Scope not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> updateScope(@PathVariable Long id, @RequestBody @Valid CreateTagDTO scopeDetails) {
    ScopeDTO updatedScope = scopeService.updateScope(id, scopeDetails);
    logger.info("Updated scope with id: {} and details: {}", id, updatedScope);
    return ResponseEntity.ok("Updated scope with ID: " + id);
  }

  /**
   * Deletes an existing scope by its ID.
   *
   * @param id the ID of the scope to be deleted
   * @return a ResponseEntity indicating the success of the delete operation
   */
  @Operation(summary = "Delete scope", description = "Deletes an existing scope by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scope deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Scope not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameter"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> deleteScope(@PathVariable Long id) {
    scopeService.deleteScope(id);
    logger.info("Deleted scope with id: {}", id);
    return ResponseEntity.ok("Deleted scope with ID: " + id);
  }

}
