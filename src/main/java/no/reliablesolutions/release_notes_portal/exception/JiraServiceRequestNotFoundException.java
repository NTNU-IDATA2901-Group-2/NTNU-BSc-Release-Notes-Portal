package no.reliablesolutions.release_notes_portal.exception;

/**
 * Exception thrown when a Jira issue with the specified key is not found.
 */
public class JiraServiceRequestNotFoundException extends RuntimeException {
  private final String issueKey;

  public JiraServiceRequestNotFoundException(String issueKey) {
    super("Jira issue with key " + issueKey + " not found");
    this.issueKey = issueKey;
  }

  public JiraServiceRequestNotFoundException(String issueKey, Throwable cause) {
    super("Jira issue with key " + issueKey + " not found", cause);
    this.issueKey = issueKey;
  }

  public String getIssueKey() {
    return issueKey;
  }
}
