package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ChangeNoteFilterOptionsDTO;
import no.reliablesolutions.release_notes_portal.dto.CreateChangeNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.PaginatedResponseDTO;
import no.reliablesolutions.release_notes_portal.service.ChangeNoteService;

/**
 * Controller for managing change note endpoints.
 */
@Tag(name = "Change Notes", description = "Endpoints for managing change notes")
@RestController
@RequestMapping("/api/changenotes")
@AllArgsConstructor
public class ChangeNoteController {
  private final ChangeNoteService changeNoteService;

  private final Logger logger = LoggerFactory.getLogger(ChangeNoteController.class);

  /**
   * Endpoint for creating a new change note with the provided details.
   * 
   * @param createChangeNoteDTO the details of the change note to be created
   * @return a ResponseEntity containing the ID of the created change note
   */
  @Operation(summary = "Create change note", description = "Creates a new change note with provided details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Change note created successfully"),
      @ApiResponse(responseCode = "404", description = "Related entity not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> createChangeNote(
      @RequestBody(required = false) CreateChangeNoteDTO createChangeNoteDTO) {
    long id = changeNoteService.createChangeNoteFromDto(createChangeNoteDTO);
    logger.info("Change note created with id: {}", id);
    return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
  }

  /**
   * Endpoint for archiving an existing change note by its ID.
   * 
   * @param id the ID of the change note to be archived
   * @return a ResponseEntity indicating the result of the archive operation
   */
  @Operation(summary = "Archive change note", description = "Archives an existing change note")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Change note archived successfully"),
      @ApiResponse(responseCode = "404", description = "Change note not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/archive")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> archiveChangeNote(@PathVariable long id) {
    changeNoteService.archiveChangeNote(id);
    logger.info("Change note archived with id: {}", id);
    return ResponseEntity.ok().body("Change note archived successfully");
  }

  /**
   * Endpoint for retrieving a list of all change notes with optional filters.
   *
   * @param filterOptions the filter options for querying change notes
   * @param page          the zero-based page index to retrieve, or {@code null}
   *                      to return all matching change notes unpaged
   * @param size          the page size, or {@code null} to return all matching
   *                      change notes unpaged
   * @return a ResponseEntity with a 200 status and a {@link PaginatedResponseDTO}
   *         wrapping the page of ChangeNoteDTOs and the total item count
   */
  @Operation(summary = "Get all change notes, with optional filters", description = "Retrieves a list of all change notes with optional filters")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Change notes retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("")
  public ResponseEntity<PaginatedResponseDTO<List<ChangeNoteDTO>>> getAllChangeNotes(
      @ModelAttribute ChangeNoteFilterOptionsDTO filterOptions,
      @RequestParam (required = false) Integer page,
      @RequestParam (required = false) Integer size) {
    PaginatedResponseDTO<List<ChangeNoteDTO>> changeNotes = changeNoteService.getAllChangeNotes(filterOptions, page, size);
    logger.info("Retrieved {} change notes with filters: {} and pagination parameters: Page {}, Size {}", changeNotes.content().size(), filterOptions, page, size);
    return ResponseEntity.ok(changeNotes);
  }

  /**
   * Endpoint for retrieving details of a specific change note by its ID.
   * 
   * @param id the ID of the change note to retrieve
   * @return a ResponseEntity containing the ChangeNoteDTO representing the
   *         retrieved change note
   */
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

  /**
   * Endpoint for updating an existing change note with new details.
   * 
   * @param id                  the ID of the change note to update
   * @param createChangeNoteDTO the new details for the change note
   * @return a ResponseEntity containing the updated ChangeNoteDTO representing
   *         the updated change note
   */
  @Operation(summary = "Update change note", description = "Updates an existing change note with new details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Change note updated successfully"),
      @ApiResponse(responseCode = "404", description = "Change note not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ChangeNoteDTO> updateChangeNote(@PathVariable long id,
      @RequestBody CreateChangeNoteDTO createChangeNoteDTO) {
    ChangeNoteDTO changeNote = changeNoteService.updateChangeNote(id, createChangeNoteDTO);
    logger.info("Updated change note with id: {}", id);
    return ResponseEntity.ok(changeNote);
  }

  /**
   * Endpoint for publishing an existing change note by its ID.
   * 
   * @param id      the ID of the change note to be published
   * @param publish a boolean value indicating whether to publish (true) or
   *                unpublish (false) the change note
   * @return a ResponseEntity indicating the result of the publish operation
   */
  @Operation(summary = "Publish change note", description = "Publishes an existing change note by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Change note published successfully"),
      @ApiResponse(responseCode = "404", description = "Change note not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/publish")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> publishChangeNote(@PathVariable long id, @Valid @RequestParam boolean publish) {
    changeNoteService.publishChangeNote(id, publish);
    logger.info("Published change note with id: {}", id);
    return ResponseEntity.ok().body("Change note published successfully");
  }
 
  /**
   * Endpoint to check if a list of change notes has associated git commit hashes.
   * 
   * @param ids a list of IDs of the change notes to check for associated git commit hashes
   * @return a ResponseEntity containing a boolean value indicating whether the
   *         change notes have associated git commit hashes
   */
  @Operation(summary = "Check if change notes have associated git commits", description = "Checks if the change notes with the given IDs have associated git commit hashes")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Check completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/has-commits/{ids}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Boolean> hasCommitHash(@PathVariable List<Long> ids) {
      boolean hasCommits = changeNoteService.hasCommitHash(ids);
      
      logger.info("Checked for git commits for change notes with ids: {}. Result: {}", ids, hasCommits);

      return ResponseEntity.ok().body(hasCommits);
  }

}
