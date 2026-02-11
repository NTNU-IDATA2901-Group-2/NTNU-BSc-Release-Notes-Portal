package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CreateChangeNoteDTO;
import com.example.demo.service.ChangeNoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
  public ResponseEntity<String> createChangeNote(@RequestBody CreateChangeNoteDTO createChangeNoteDTO) {
    long id = changeNoteService.createChangeNote(createChangeNoteDTO);
      logger.info("Change note created with id: {}", id);
      return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
  }
}
