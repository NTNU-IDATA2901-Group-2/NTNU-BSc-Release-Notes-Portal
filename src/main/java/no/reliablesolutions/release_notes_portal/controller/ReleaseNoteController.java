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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.dto.CreateReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.PaginatedResponseDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteDTO;
import no.reliablesolutions.release_notes_portal.dto.ReleaseNoteFilterOptionsDTO;
import no.reliablesolutions.release_notes_portal.service.ReleaseNoteService;

/**
 * Controller for managing release notes. Provides methods for creating,
 * archiving, retrieving, and updating release notes.
 */
@Tag(name = "Release Notes", description = "Endpoints for managing release notes")
@RestController
@RequestMapping("/api/releasenotes")
@AllArgsConstructor
public class ReleaseNoteController {

  private final ReleaseNoteService releaseNoteService;

  private final Logger logger = LoggerFactory.getLogger(ReleaseNoteController.class);

  /**
   * Creates a new release note with the provided details.
   *
   * @param createReleaseNoteDTO the DTO containing details for the new release
   *                             note
   * @return a ResponseEntity with a 201 status and the ID of the created release
   *         note in the body
   */
  @Operation(summary = "Create release note", description = "Creates a new release note with provided details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Release note created successfully"),
      @ApiResponse(responseCode = "404", description = "Related entity not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> createReleaseNote(
      @Parameter(name = "createReleaseNoteDTO", description = "Details of the release note to be created", required = true) @Valid @RequestBody CreateReleaseNoteDTO createReleaseNoteDTO) {
    long id = releaseNoteService.createReleaseNote(createReleaseNoteDTO);
    logger.info("Release note created with id: {}", id);
    return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
  }

  /**
   * Archives an existing release note by its ID.
   *
   * @param id the ID of the release note to be archived
   * @return a ResponseEntity with a 200 status and a message indicating that the
   *         release note was archived successfully
   */
  @Operation(summary = "Archive release note", description = "Archives an existing release note")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Release note archived successfully"),
      @ApiResponse(responseCode = "404", description = "Release note not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request variable"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/archive")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> archiveReleaseNote(@PathVariable long id) {
    releaseNoteService.archiveReleaseNote(id);
    logger.info("Release note archived with id: {}", id);
    return ResponseEntity.ok().body("Release note archived successfully");
  }

  /**
   * Retrieves a list of all non-archived release notes, with optional filters for
   * query, published status, and product.
   *
   * @param filterOptions the filter options for querying release notes
   * @param page          the zero-based page index to retrieve, or {@code null}
   *                      to return all matching release notes unpaged
   * @param size          the page size, or {@code null} to return all matching
   *                      release notes unpaged
   * @return a ResponseEntity with a 200 status and a {@link PaginatedResponseDTO}
   *         wrapping the page of ReleaseNoteDTOs and the total item count
   */
  @Operation(summary = "Get all release notes, with optional filters", description = "Retrieves a list of all release notes with optional filters")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Release notes retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request variable"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("")
  public ResponseEntity<PaginatedResponseDTO<List<ReleaseNoteDTO>>> getAllReleaseNotes(
      @ModelAttribute ReleaseNoteFilterOptionsDTO filterOptions,
      @RequestParam (required = false) Integer page,
      @RequestParam (required = false) Integer size) {
    PaginatedResponseDTO<List<ReleaseNoteDTO>> releaseNotes = releaseNoteService.getAllReleaseNotes(filterOptions, page, size);
    logger.info("Retrieved {} release notes with filters: {} and pagination parameters: Page {}, Size {}", releaseNotes.content().size(), filterOptions, page, size);
    return ResponseEntity.ok(releaseNotes);
  }

  /**
   * Retrieves details of a specific non-archived release note by its ID.
   *
   * @param id the ID of the release note to be retrieved
   * @return ResponseEntity with ReleaseNoteDTO in the body if found
   */
  @Operation(summary = "Get release note by ID", description = "Retrieves details of a specific release note by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Release note retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Release note not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request variable"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ReleaseNoteDTO> getReleaseNoteById(@PathVariable long id) {
    ReleaseNoteDTO releaseNote = releaseNoteService.getReleaseNoteById(id);
    logger.info("Retrieved release note with id: {}", id);
    return ResponseEntity.ok(releaseNote);
  }

  /**
   * Diffs two release notes and returns the release notes that are new since
   * the earlier of the two. The two notes must belong to the same product.
   *
   * @param releaseNoteOneId the ID of one release note to diff
   * @param releaseNoteTwoId the ID of the other release note to diff
   * @return a ResponseEntity with a 200 status and the list of ReleaseNoteDTOs in
   *         the range, ordered by creation time descending
   */
  @Operation(summary = "Diff release notes", description = "Returns the release notes that are new since the earlier of two release notes of the same product")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Release notes diffed successfully"),
      @ApiResponse(responseCode = "404", description = "Release note not found"),
      @ApiResponse(responseCode = "400", description = "Missing parameters or release notes of different products"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/diff")
  public ResponseEntity<List<ReleaseNoteDTO>> diffReleaseNotes(
      @RequestParam(required = true) Long releaseNoteOneId,
      @RequestParam(required = true) Long releaseNoteTwoId) {
    List<ReleaseNoteDTO> releaseNotes = releaseNoteService.getReleaseNotesBetween(releaseNoteOneId, releaseNoteTwoId);
    logger.info("Diffed release notes {} and {}, found {} release note(s) in range", releaseNoteOneId,
        releaseNoteTwoId, releaseNotes.size());
    return ResponseEntity.ok(releaseNotes);
  }

  /**
   * Updates an existing release note with new details.
   *
   * @param id                   the ID of the release note to be updated
   * @param createReleaseNoteDTO the DTO containing updated details for the
   *                             release note
   * @return a ResponseEntity with a 200 status and a ReleaseNoteDTO in the body
   *         representing the updated release note
   */
  @Operation(summary = "Update release note", description = "Updates an existing release note with new details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Release note updated successfully"),
      @ApiResponse(responseCode = "404", description = "Release note not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ReleaseNoteDTO> updateReleaseNote(@PathVariable long id,
      @Valid @RequestBody CreateReleaseNoteDTO createReleaseNoteDTO) {
    ReleaseNoteDTO releaseNote = releaseNoteService.updateReleaseNote(id, createReleaseNoteDTO);
    logger.info("Updated release note with id: {}", id);
    return ResponseEntity.ok(releaseNote);
  }

  /**
   * Publishes an existing release note by its ID. Reverts release note to draft
   * if publish is false.
   *
   * @param id the ID of the release note to be published
   * @param publish a boolean indicating whether to publish (true) or set as draft (false)
   * @return a ResponseEntity with the ID of the published release note in the body
   */
  @Operation(summary = "Publish release note", description = "Publishes an existing release note by its ID. Reverts release note to draft if publish is false.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Release note published successfully"),
      @ApiResponse(responseCode = "404", description = "Release note not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request variable"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/publish")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Long> publishReleaseNote(@PathVariable long id,
      @Valid @RequestParam(required = true) boolean publish) {
    releaseNoteService.publishReleaseNote(id, publish);
    logger.info("Release note with id {} published: {}", id, publish);
    return ResponseEntity.ok(id);
  }
}
