package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ChangeNoteDTO;
import com.example.demo.dto.CreateChangeNoteDTO;
import com.example.demo.service.ChangeNoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Change Notes", description = "Endpoints for managing change notes")
@RestController
@RequestMapping("/api/changenotes")
@AllArgsConstructor
public class ChangeNoteController {
  private final ChangeNoteService changeNoteService;

  private final Logger logger = LoggerFactory.getLogger(ChangeNoteController.class);

  @Operation(summary = "Create change note", description = "Creates a new change note with provided details")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Change note created successfully"),
    @ApiResponse(responseCode = "404", description = "Related entity not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  public ResponseEntity<String> createChangeNote(@RequestBody(required = false) CreateChangeNoteDTO createChangeNoteDTO) {
    long id = changeNoteService.createChangeNote(createChangeNoteDTO);
      logger.info("Change note created with id: {}", id);
      return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
  }


  @Operation(summary = "Archive change note", description = "Archives an existing change note")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Change note archived successfully"),
    @ApiResponse(responseCode = "404", description = "Change note not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/archive")
  public ResponseEntity<String> archiveChangeNote(@PathVariable long id) {
    changeNoteService.archiveChangeNote(id);
    logger.info("Change note archived with id: {}", id);
    return ResponseEntity.ok().body("Change note archived successfully");
  }


  @Operation(summary = "Get all change notes", description = "Retrieves a list of all change notes")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Change notes retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("")
  public ResponseEntity<List<ChangeNoteDTO>> getAllChangeNotes(
    @RequestParam(required = false) Boolean published,
    @RequestParam(required = false) Long customerId,
    @RequestParam(required = false) Long featureId,
    @RequestParam(required = false) Long scopeId,
    @RequestParam(required = false) Long productId
    ) {
    List<ChangeNoteDTO> changeNotes = changeNoteService.getAllChangeNotes(published, customerId, featureId, scopeId, productId);
    logger.info("Retrieved {} change notes with filters - published: {}, customerId: {}, featureId: {}, scopeId: {}, productId: {}", changeNotes.size(), published, customerId, featureId, scopeId, productId);
    return ResponseEntity.ok(changeNotes);
  }

  @Operation(summary = "Get change note by ID", description = "Retrieves details of a specific change note by its ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Change note retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Change note not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ChangeNoteDTO> getChangeNoteById(@PathVariable long id) {
    ChangeNoteDTO changeNote = changeNoteService.getChangeNoteById(id);
    logger.info("Retrieved change note with id: {}", id);
    return ResponseEntity.ok(changeNote);
  }


  @Operation(summary = "Update change note", description = "Updates an existing change note with new details")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Change note updated successfully"),
    @ApiResponse(responseCode = "404", description = "Change note not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ChangeNoteDTO> updateChangeNote(@PathVariable long id, @RequestBody CreateChangeNoteDTO createChangeNoteDTO) {
    ChangeNoteDTO changeNote = changeNoteService.updateChangeNote(id, createChangeNoteDTO);
    logger.info("Updated change note with id: {}", id);
    return ResponseEntity.ok(changeNote);
  }


  @Operation(summary = "Publish change note", description = "Publishes an existing change note by its ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Change note published successfully"),
    @ApiResponse(responseCode = "404", description = "Change note not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/publish")
  public ResponseEntity<String> publishChangeNote(@PathVariable long id, @Valid @RequestParam boolean publish) {
    changeNoteService.publishChangeNote(id, publish);
    logger.info("Published change note with id: {}", id);
    return ResponseEntity.ok().body("Change note published successfully");
  }
}
