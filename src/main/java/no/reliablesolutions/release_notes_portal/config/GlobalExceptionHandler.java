package no.reliablesolutions.release_notes_portal.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import no.reliablesolutions.release_notes_portal.exception.ChangeNoteHasNoGitCommitsException;
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.CustomerNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.DiffStringGenerationException;
import no.reliablesolutions.release_notes_portal.exception.FailedSyncGitChangeNotesException;
import no.reliablesolutions.release_notes_portal.exception.FailedToSaveEntityException;
import no.reliablesolutions.release_notes_portal.exception.FeatureNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ProductNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ReleaseNoteNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.ScopeNotFoundException;

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
   * Handles the case where syncing Git change notes fails. Logs the event and returns a 500 response with a message indicating the failure to sync Git change notes.
   * @param e the exception containing details about the failure to sync Git change notes
   * @return a ResponseEntity with a 500 status and a message indicating the failure to sync Git change notes
   */
  @ExceptionHandler(value = {FailedSyncGitChangeNotesException.class})
  public ResponseEntity<String> handleFailedSyncGitChangeNotesException(FailedSyncGitChangeNotesException e) {
    logger.warn("Failed to sync Git change notes", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to sync Git change notes: " + e.getMessage());
  }

  /**
   * Handles the case where a data integrity violation occurs. Logs the event and returns a 500 response with a message indicating the data integrity violation.
   * @param e the exception containing details about the data integrity violation
   * @return a ResponseEntity with a 500 status and a message indicating the data integrity violation
   */
  @ExceptionHandler(value = {DataIntegrityViolationException.class})
  public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    logger.warn("Data integrity violation: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Data integrity violation: " + e.getMessage());
  }
  
  /**
   * Handles the case where a change note has no associated git commits. Logs the event and returns a 400 response with a message indicating that the change note has no associated git commits.
   * @param e the exception containing details about the change note with no git commits
   * @return a ResponseEntity with a 400 status and a message indicating that the change note has no associated git commits
   */
  @ExceptionHandler(value = {ChangeNoteHasNoGitCommitsException.class})
  public ResponseEntity<String> handleChangeNoteHasNoGitCommitsException(ChangeNoteHasNoGitCommitsException e) {
    logger.warn("Change note has no associated git commits: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  /**
   * Handles the case where there is an error during the generation of a diff string. Logs the event and returns a 500 response with a message indicating the error generating the diff string.
   * @param e the exception containing details about the error generating the diff string
   * @return a ResponseEntity with a 500 status and a message indicating the error generating the diff string
   */
  @ExceptionHandler(value = {DiffStringGenerationException.class})
  public ResponseEntity<String> handleDiffStringGenerationException(DiffStringGenerationException e) {
    logger.warn("Error generating diff string: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating diff string");
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
   * Handles the case where method argument validation fails. Logs the event and returns a 400 response with a message indicating the validation error.
   * @param e the exception containing details about the method argument validation failure
   * @return a ResponseEntity with a 400 status and a message indicating the validation error
   */
  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    logger.warn("Validation failed: {}", e.getMessage());
    String errorMessage = e.getBindingResult().getFieldErrors().stream()
        .map(error -> String.format("Field '%s' %s", error.getField(), error.getDefaultMessage()))
        .findFirst()
        .orElse("Validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
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
   * Handles the case where an illegal state is encountered. Logs the event and returns a 500 response with a message indicating that there is an illegal state.
   * @param e the exception containing details about the illegal state
   * @return a ResponseEntity with a 500 status and a message indicating that there is an illegal state
   */
  @ExceptionHandler(value = {IllegalStateException.class})
  public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
    logger.warn("Illegal state: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Illegal internal server state");
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
   * Handles the case where a requested resource is not found. Logs the event and returns a 404 response with a message.
   * @param e the exception containing details about the missing resource
   * @return a ResponseEntity with a 404 status and a message indicating the resource was not found
   */
  @ExceptionHandler(value = {NoResourceFoundException.class})
  public ResponseEntity<String> handleResourceNotFoundException(NoResourceFoundException e) {
    logger.warn("Resource not found: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found: " + e.getMessage());
  }

  /**
   * Handles the case where a Git repository is not found. Logs the event and returns a 404 response with a message.
   * @param e the exception containing details about the missing Git repository
   * @return a ResponseEntity with a 404 status and a message indicating the Git repository
   */
  @ExceptionHandler(value = {no.reliablesolutions.release_notes_portal.exception.GitRepositoryNotFoundException.class})
  public ResponseEntity<String> handleGitRepositoryNotFoundException(no.reliablesolutions.release_notes_portal.exception.GitRepositoryNotFoundException e) {
    logger.warn("Git repository not found: {}", e.getGitRepositoryId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Git repository with ID %d not found", e.getGitRepositoryId()));
  }

  /**
   * Handles the case where authorization is denied. Logs the event and returns a 403 response with a message indicating that authorization was denied.
  *
   * @param e the exception containing details about the authorization denial
   * @return a ResponseEntity with a 403 status and a message indicating that authorization was denied
   */
  @ExceptionHandler(value = {AuthorizationDeniedException.class})
  public ResponseEntity<String> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
    logger.warn("Authorization denied: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization denied: " + e.getMessage());
  }

  /**
   * Handles the case where a locale is not supported. Logs the event and returns a 400 response with a message indicating the unsupported locale and the supported locales.
   * @param e the exception containing details about the unsupported locale
   * @return a ResponseEntity with a 400 status and a message indicating the unsupported locale and the supported locales
   */
  @ExceptionHandler(value = {no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException.class})
  public ResponseEntity<String> handleLocaleNotSupportedException(no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException e) {
    logger.warn("Locale not supported: {}", e.getLocale());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Locale '%s' is not supported. Supported locales are: en (English), no (Norwegian Bokmål), fr (French)", e.getLocale()));
  }

  /**
   * Handles the case where a non-transient AI exception occurs. Logs the event and returns a 500 response with a message indicating the non-transient AI exception.
   * @param e the exception containing details about the non-transient AI exception
   * @return a ResponseEntity with a 500 status and a message indicating the non-transient AI exception
   */
  @ExceptionHandler(value = {NonTransientAiException.class})
  public ResponseEntity<String> handleNonTransientAiException(NonTransientAiException e) {
    logger.warn("Non-transient AI exception: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Non-transient AI exception: " + e.getMessage());
  }

  /**
   * Handles the case where a request method is not supported. Logs the event and returns a 400 response with a message indicating the unsupported request method.
   * @param e the exception containing details about the unsupported request method
   * @return a ResponseEntity with a 400 status and a message indicating the unsupported request method
   */
  @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
  public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
    logger.warn("Request method not supported: {}", e.getMethod());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request method not supported: " + e.getMethod());
  }
}
