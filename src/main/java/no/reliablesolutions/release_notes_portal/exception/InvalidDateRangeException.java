package no.reliablesolutions.release_notes_portal.exception;

import java.time.LocalDate;

import lombok.Getter;

/**
 * Exception thrown when a date range is invalid because its start date is after its end date.
 */
@Getter
public class InvalidDateRangeException extends RuntimeException {
  final LocalDate fromDate;
  final LocalDate toDate;

  /**
   * Constructs a new InvalidDateRangeException for the given range bounds.
   *
   * @param fromDate the start of the range
   * @param toDate   the end of the range, which was before {@code fromDate}
   */
  public InvalidDateRangeException(LocalDate fromDate, LocalDate toDate) {
    super("Invalid date range: fromDate (" + fromDate + ") must not be after toDate (" + toDate + ")");
    this.fromDate = fromDate;
    this.toDate = toDate;
  }
}
