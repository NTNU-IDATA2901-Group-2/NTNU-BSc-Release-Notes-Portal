package no.reliablesolutions.release_notes_portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main application class for the Release Notes application.
 */
@SpringBootApplication
public class ReleaseNotesApp {

  /**
   * The entry point of the application, which starts the Spring Boot application context.
   * @param args
   */
	public static void main(String[] args) {
		SpringApplication.run(ReleaseNotesApp.class, args);
	}
}
