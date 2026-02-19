package com.example.demo.exception;

import lombok.Getter;

@Getter
public class ChangeNoteNotFoundException extends RuntimeException {
  final long changeNoteId;

  public ChangeNoteNotFoundException(long id) {
    super("Change note not found with id: " + id);
    this.changeNoteId = id;
  }
}
