package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.exception.CustomerNotFoundException;
import com.example.demo.exception.FeatureNotFoundException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.exception.ScopeNotFoundException;

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
}
