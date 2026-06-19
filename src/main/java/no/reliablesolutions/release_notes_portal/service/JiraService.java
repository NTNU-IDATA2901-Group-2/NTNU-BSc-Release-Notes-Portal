package no.reliablesolutions.release_notes_portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import no.reliablesolutions.release_notes_portal.exception.JiraServiceRequestNotFoundException;
import no.reliablesolutions.release_notes_portal.exception.JiraCommunicationException;
import tools.jackson.databind.JsonNode;

@Service
public class JiraService {
  private static final Logger logger = LoggerFactory.getLogger(JiraService.class);
  private final String JIRA_BASE_URL;
  private final String JIRA_EMAIL;
  private final String JIRA_API_TOKEN;
  private final RestClient restClient;

  public JiraService(
      @Value("${JIRA_BASE_URL}") String jiraBaseUrl,
      @Value("${JIRA_EMAIL}") String jiraEmail,
      @Value("${JIRA_API_TOKEN}") String jiraApiToken) {
    this.JIRA_BASE_URL = jiraBaseUrl;
    this.JIRA_EMAIL = jiraEmail;
    this.JIRA_API_TOKEN = jiraApiToken;
    restClient = RestClient
        .builder()
        .baseUrl(JIRA_BASE_URL)
        .defaultHeaders(h -> h.setBasicAuth(JIRA_EMAIL, JIRA_API_TOKEN))
        .build();

  }

  public String getServiceRequest(String issueKey) {
    String propertyName = "/fields/issuelinks/0/inwardIssue/key"; // Assuming where the service request key is located in Jira issue JSON structure
    try {
      JsonNode body = restClient.get()
          .uri("/rest/api/latest/issue/{key}", issueKey)
          .retrieve()
          .body(JsonNode.class);

      if (body == null) {
        throw new JiraServiceRequestNotFoundException(issueKey);
      }

      JsonNode sr = body.at(propertyName);
      if (sr.isMissingNode()) {
        throw new JiraServiceRequestNotFoundException(issueKey);
      }
      logger.info("Successfully fetched Jira issue with key {}: Service Request {}", issueKey, sr);
      return sr.asString();

    } catch (RestClientResponseException e) {
      // Jira returns 404 when the issue doesn't exist or the token can't see it; anything
      // else (401/403/429/5xx) is an upstream/communication problem, not a missing issue.
      if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
        throw new JiraServiceRequestNotFoundException(issueKey, e);
      }
      throw new JiraCommunicationException(issueKey, e);
    } catch (JiraServiceRequestNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new JiraCommunicationException(issueKey, e);
    }
  }

}
