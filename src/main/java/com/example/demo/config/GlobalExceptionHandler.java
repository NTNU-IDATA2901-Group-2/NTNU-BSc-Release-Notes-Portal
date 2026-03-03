package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.demo.exception.ChangeNoteAlreadyHasReleaseNoteException;
import com.example.demo.exception.ChangeNoteNotFoundException;
import com.example.demo.exception.CustomerNotFoundException;
import com.example.demo.exception.FailedToSaveEntityException;
import com.example.demo.exception.FeatureNotFoundException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.exception.ReleaseNoteNotFoundException;
import com.example.demo.exception.ScopeNotFoundException;

/**
 * Global exception handler for the application. Catches specific exceptions thrown by controllers and services, logs the events, and returns appropriate HTTP responses with messages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
  Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles the case where a customer is not found. Logs the event and returns a 404 response with a message indicating the customer was not found.
   * @param e the exception containing details about the missing customer
   * @return a ResponseEntity with a 404 status and a message indicating the customer was not found
   */
  @ExceptionHandler(value = {CustomerNotFoundException.class})
  public ResponseEntity<String> handleCustomerNotFoundException(CustomerNotFoundException e) {
    logger.warn("Customer not found: {}", e.getCustomerId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Customer with ID %d not found", e.getCustomerId()));
  }

  /**
   * Handles the case where a feature is not found. Logs the event and returns a 404 response with a message indicating the feature was not found.
   * @param e the exception containing details about the missing feature
   * @return a ResponseEntity with a 404 status and a message indicating the feature was not found
   */
  @ExceptionHandler(value = {FeatureNotFoundException.class})
  public ResponseEntity<String> handleFeatureNotFoundException(FeatureNotFoundException e) {
    logger.warn("Feature not found: {}", e.getFeatureId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Feature with ID %d not found", e.getFeatureId()));
  }

  /**
   * Handles the case where a product is not found. Logs the event and returns a 404 response with a message indicating the product was not found.
   * @param e the exception containing details about the missing product
   * @return a ResponseEntity with a 404 status and a message indicating the product was not found
   *
   */
  @ExceptionHandler(value = {ProductNotFoundException.class})
  public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException e) {
    logger.warn("Product not found: {}", e.getProductId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Product with ID %d not found", e.getProductId()));
  }

  /**
   * Handles the case where a scope is not found. Logs the event and returns a 404 response with a message indicating the scope was not found.
   * @param e the exception containing details about the missing scope
   * @return a ResponseEntity with a 404 status and a message indicating the scope was not found
   */
  @ExceptionHandler(value = {ScopeNotFoundException.class})
  public ResponseEntity<String> handleScopeNotFoundException(ScopeNotFoundException e) {
    logger.warn("Scope not found: {}", e.getScopeId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Scope with ID %d not found", e.getScopeId()));
  }


  /**
   * Handles the case where a message is not readable. Logs the event and returns a 400 response with a message indicating the message is not readable.
   * @param e the exception containing details about the unreadable message
   * @return a ResponseEntity with a 400 status and a message indicating that the message is not readable
   */
  @ExceptionHandler(value = {HttpMessageNotReadableException.class})
  public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    logger.warn("Message not readable: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message not readable");
  }

  /**
   * Handles the case where a method argument type mismatch occurs. Logs the event and returns a 400 response with a message indicating the invalid argument type.
   * @param e the exception containing details about the method argument type mismatch
   * @return a ResponseEntity with a 400 status and a message indicating the invalid argument type
   */
  @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
  public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    logger.warn("Invalid argument type: {}", e.getMessage());

    Class<?> requiredType = e.getRequiredType();
    if (requiredType == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument type: " + e.getName() + " has an invalid value");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument type: " + e.getName() + " should be of type " + requiredType.getSimpleName());
  }

  /**
   * Handles any unexpected exceptions that occur during request processing. Logs the error and returns a generic 500 response.
   * @param e the exception that was thrown
   * @return a ResponseEntity with a 500 status and a generic error message
   */
  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<String> handleGenericException(Exception e) {
    logger.error("An unexpected error occurred", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
  }

  /**
   * Handles the case where a change note is not found. Logs the event and returns a 404 response with a message.
   * @param e the exception containing details about the missing change note
   * @return a ResponseEntity with a 404 status and a message indicating the change note was not found
    *
   */
  @ExceptionHandler(value = {ChangeNoteNotFoundException.class})
  public ResponseEntity<String> handleChangeNoteNotFoundException(ChangeNoteNotFoundException e) {
    logger.warn("Change note not found: {}", e.getChangeNoteId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Change note with ID %d not found", e.getChangeNoteId()));
  }

  /**
   * Handles the case where an illegal argument is provided. Logs the event and returns a 400 response with a message.
   * @param e the exception containing details about the illegal argument
   * @return a ResponseEntity with a 400 status and a message indicating the illegal argument
   */
  @ExceptionHandler(value = {IllegalArgumentException.class})
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
    logger.warn("Invalid argument: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument: " + e.getMessage());
  }

  /**
   * Handles the case where an entity failed to save. Logs the event and returns a 500 response with a message.
   * @param e the exception containing details about the failure to save the entity
   * @return a ResponseEntity with a 500 status and a message indicating the failure to save the entity
   */
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

  /**
   * Handles the case where a change note already has an associated release note. Logs the event and returns a 400 response with a message.
   * @param e the exception containing details about the change note and existing release note
   * @return a ResponseEntity with a 400 status and a message indicating the change note already has a release note
   */
  @ExceptionHandler(value = {ChangeNoteAlreadyHasReleaseNoteException.class})
  public ResponseEntity<String> handleChangeNoteAlreadyHasReleaseNoteException(ChangeNoteAlreadyHasReleaseNoteException e) {
    logger.warn("Change note already has a release note: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Change note with ID " + e.getChangeNoteId() + " already has a release note with ID " + e.getExistingReleaseNoteId());
  }

  /**
   * Handles the case where a requested resource is not found. Logs the event and returns a 404 response with a message.
   * @param e the exception containing details about the missing resource
   * @return a ResponseEntity with a 404 status and a message indicating the resource was not found
   */
  @ExceptionHandler(value = {NoResourceFoundException.class})
  public ResponseEntity<String> handleResourceNotFoundException(NoResourceFoundException e) {
    logger.warn("Resource not found: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found: " + e.getMessage());
  }
}
