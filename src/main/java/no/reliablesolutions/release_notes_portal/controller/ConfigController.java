package no.reliablesolutions.release_notes_portal.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller for managing public configuration endpoints.
 */
@RestController
@RequestMapping("/api/public/config")
@Profile({ "dev", "prod" })
public class ConfigController {

  @Value("${OIDC_ISSUER_URI}")
  private String oidcIssuerUri;

  @Value("${OIDC_CLIENT_ID}")
  private String oidcClientId;

  @Value("${OIDC_SCOPES:openid profile}")
  private String oidcScopes;

  @Value("${OIDC_ROLES_CLAIM:roles}")
  private String oidcRolesClaim;

  @Value("${JIRA_BASE_URL}")
  private String jiraBaseUrl;

  /**
   * Endpoint to retrieve public configuration values for the frontend, such as
   * OIDC settings.
   *
   * @return A map containing the configuration values.
   */
  @Operation(summary = "Get configuration", description = "Returns public configuration values for the frontend")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<Map<String, String>> getConfig() {
    Map<String, String> env = Map.of(
        "OIDC_ISSUER_URI", oidcIssuerUri,
        "OIDC_CLIENT_ID", oidcClientId,
        "OIDC_SCOPES", oidcScopes,
        "OIDC_ROLES_CLAIM", oidcRolesClaim,
        "JIRA_BASE_URL", jiraBaseUrl);

    return ResponseEntity.ok(env);
  }
}
