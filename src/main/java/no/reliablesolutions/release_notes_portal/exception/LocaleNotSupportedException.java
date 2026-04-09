package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

/**
 * Exception thrown when a locale is not supported.
 */
@Getter
public class LocaleNotSupportedException extends RuntimeException {
  final String locale;

  /**
   * Constructs a new LocaleNotSupportedException with the specified locale.
   *
   * @param locale the name of the locale that is not supported
   */
  public LocaleNotSupportedException(String locale) {
    super("Locale not supported: " + locale);
    this.locale = locale;
  }
}
