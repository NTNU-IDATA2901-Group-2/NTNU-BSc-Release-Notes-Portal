package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when there is a communication failure with Jira.
 */
public class JiraCommunicationException extends RuntimeException {
  private final String issueKey;

  public JiraCommunicationException(String issueKey, Throwable cause) {
    super("Failed to communicate with Jira for issue key " + issueKey + ": " + cause.getMessage(), cause);
    this.issueKey = issueKey;
  }

  public String getIssueKey() {
    return issueKey;
  }
}
