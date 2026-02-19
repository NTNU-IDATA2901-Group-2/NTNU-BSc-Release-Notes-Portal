package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.exception.ChangeNoteNotFoundException;
import com.example.demo.exception.CustomerNotFoundException;
import com.example.demo.exception.FailedToSaveEntityException;
import com.example.demo.exception.FeatureNotFoundException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.exception.ReleaseNoteNotFoundException;
import com.example.demo.exception.ScopeNotFoundException;
import com.example.demo.exception.ChangeNoteAlreadyHasReleaseNoteException;

@ControllerAdvice
public class GlobalExceptionHandler {
  Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(value = {CustomerNotFoundException.class})
  public ResponseEntity<String> handleCustomerNotFoundException(CustomerNotFoundException e) {
    logger.warn("Customer not found: {}", e.getCustomerId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Customer with ID %d not found", e.getCustomerId()));
  }

  @ExceptionHandler(value = {FeatureNotFoundException.class})
  public ResponseEntity<String> handleFeatureNotFoundException(FeatureNotFoundException e) {
    logger.warn("Feature not found: {}", e.getFeatureId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Feature with ID %d not found", e.getFeatureId()));
  }

  @ExceptionHandler(value = {ProductNotFoundException.class})
  public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException e) {
    logger.warn("Product not found: {}", e.getProductId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Product with ID %d not found", e.getProductId()));
  }

  @ExceptionHandler(value = {ScopeNotFoundException.class})
  public ResponseEntity<String> handleScopeNotFoundException(ScopeNotFoundException e) {
    logger.warn("Scope not found: {}", e.getScopeId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Scope with ID %d not found", e.getScopeId()));
  }

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<String> handleGenericException(Exception e) {
    logger.error("An unexpected error occurred: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
  }

  @ExceptionHandler(value = {ChangeNoteNotFoundException.class})
  public ResponseEntity<String> handleChangeNoteNotFoundException(ChangeNoteNotFoundException e) {
    logger.warn("Change note not found: {}", e.getChangeNoteId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Change note with ID %d not found", e.getChangeNoteId()));
  }

  @ExceptionHandler(value = {IllegalArgumentException.class})
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
    logger.warn("Invalid argument: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument: " + e.getMessage());
  }

  @ExceptionHandler(value = {FailedToSaveEntityException.class})
  public ResponseEntity<String> handleFailedToSaveEntityException(FailedToSaveEntityException e) {
    logger.warn("Failed to save entity: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save entity: " + e.getMessage());
  }

  /**
   * Handles the case where a release note is not found. Logs the event and returns a 404 response with a message.
   *
   * @param e the exception containing details about the missing release note
   * @return a ResponseEntity with a 404 status and a message indicating the release note was not found
   */
  @ExceptionHandler (value = {ReleaseNoteNotFoundException.class})
  public ResponseEntity<String> handleReleaseNoteNotFoundException(ReleaseNoteNotFoundException e) {
    logger.warn("Release note not found: {}", e.getReleaseNoteId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Release note with ID %d not found", e.getReleaseNoteId()));
  }

  @ExceptionHandler(value = {ChangeNoteAlreadyHasReleaseNoteException.class})
  public ResponseEntity<String> handleChangeNoteAlreadyHasReleaseNoteException(ChangeNoteAlreadyHasReleaseNoteException e) {
    logger.warn("Change note already has a release note: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }
}
