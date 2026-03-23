package no.reliablesolutions.release_notes_portal.exception;

import lombok.Getter;

@Getter
public class LocaleNotSupportedException extends RuntimeException {
  final String locale;
  
  public LocaleNotSupportedException(String locale) {
    super("Locale not supported: " + locale);
    this.locale = locale;
  }
}
