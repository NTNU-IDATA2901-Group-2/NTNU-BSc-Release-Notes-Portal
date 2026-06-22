package no.reliablesolutions.release_notes_portal.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

  /**
   * Fetches the inward-linked service-request key for each given Jira issue in a single bulk
   * request.
   *
   * @param issueKeys the issue keys to look up
   * @return a map from each requested issue key to its first inward-linked service-request key.
   *         Issues with no inward link (or that Jira could not return) are absent from the map
   */
  public Map<String, String> getServiceRequests(List<String> issueKeys) {
    record BulkFetchRequest(List<String> issueIdsOrKeys, List<String> fields) {
    }
    try {
      JsonNode body = restClient.post()
          .uri("/rest/api/latest/issue/bulkfetch")
          .contentType(MediaType.APPLICATION_JSON)
          .body(new BulkFetchRequest(issueKeys, List.of("issuelinks")))
          .retrieve()
          .body(JsonNode.class);

      if (body == null) {
        throw new JiraCommunicationException(String.join(", ", issueKeys),
            new IllegalStateException("empty response body from bulkfetch"));
      }

      JsonNode issueErrors = body.at("/issueErrors");
      if (issueErrors.isArray() && !issueErrors.isEmpty()) {
        logger.warn("Jira bulkfetch returned errors for some of {}: {}", issueKeys, issueErrors);
      }

      Map<String, String> serviceRequestKeys = new LinkedHashMap<>();
      for (JsonNode issue : body.at("/issues")) {
        JsonNode issueKey = issue.at("/key");
        if (issueKey.isMissingNode()) {
          continue;
        }
        for (JsonNode link : issue.at("/fields/issuelinks")) {
          JsonNode inwardKey = link.at("/inwardIssue/key");
          if (!inwardKey.isMissingNode()) {
            serviceRequestKeys.put(issueKey.asString(), inwardKey.asString());
            break; // first inward-linked issue per Jira issue
          }
        }
      }
      logger.info("Resolved {} service request(s) for {} requested issue(s)",
          serviceRequestKeys.size(), issueKeys.size());
      if (serviceRequestKeys.isEmpty()) {
        logger.warn("No service requests found for {}. Ensure the API key is valid", issueKeys);
      }
      return serviceRequestKeys;

    } catch (JiraCommunicationException e) {
      throw e;
    } catch (Exception e) {
      throw new JiraCommunicationException(String.join(", ", issueKeys), e);
    }
  }

}
