package com.example.demo.exception;

public class FailedToSaveEntityException extends RuntimeException {
  public FailedToSaveEntityException(String message) {
    super(message);
  }
}
