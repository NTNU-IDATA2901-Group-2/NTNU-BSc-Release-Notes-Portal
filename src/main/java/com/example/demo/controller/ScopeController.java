package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ScopeDTO;
import com.example.demo.service.ScopeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Scopes", description = "Endpoints for managing scopes")
@RestController
@AllArgsConstructor
@RequestMapping("/api/scopes")
public class ScopeController {
  private final ScopeService scopeService;
  private final Logger logger = LoggerFactory.getLogger(ScopeController.class);
  
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

  @Operation(summary = "Get scope by ID", description = "Retrieves details of a specific scope by its ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Scope retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Scope not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ScopeDTO> getScopeById(@PathVariable Long id) {
    logger.info("Retrieved scope with id: {}", id);
    return ResponseEntity.ok(scopeService.getScopeById(id));
  }

  @Operation(summary = "Create scope", description = "Creates a new scope with provided details")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Scope created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request payload"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  public ResponseEntity<Long> createScope(ScopeDTO scopeDetails) {
    logger.info("Created scope with details: {}", scopeDetails);
    long createdScope = scopeService.createScope(scopeDetails);
    return ResponseEntity.ok(createdScope);
  }

  @Operation(summary = "Update scope", description = "Updates an existing scope with new details")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Scope updated successfully"),
    @ApiResponse(responseCode = "404", description = "Scope not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<String> updateScope(@PathVariable Long id, @RequestBody ScopeDTO scopeDetails) {
    ScopeDTO updatedScope = scopeService.updateScope(id, scopeDetails);
    logger.info("Updated scope with id: {} and details: {}", id, updatedScope);
    return ResponseEntity.ok("Updated scope with ID: " + id);
  }

  @Operation(summary = "Delete scope", description = "Deletes an existing scope by its ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Scope deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Scope not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteScope(@PathVariable Long id) {
    scopeService.deleteScope(id);
    logger.info("Deleted scope with id: {}", id);
    return ResponseEntity.ok("Deleted scope with ID: " + id);
  }

}
