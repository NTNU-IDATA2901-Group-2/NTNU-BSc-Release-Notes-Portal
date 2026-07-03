package no.reliablesolutions.release_notes_portal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtRolesGrantedAuthoritiesConverterTest {
  private static final AuthClaimsProperties KEYCLOAK_STYLE = new AuthClaimsProperties(
      "roles", "groups", "/Customers/");
  private static final AuthClaimsProperties ENTRA_STYLE = new AuthClaimsProperties(
      "roles", "roles", "Customer:");

  private Jwt jwt(Map<String, Object> claims) {
    Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "RS256").claim("sub", "user");
    claims.forEach(builder::claim);
    return builder.build();
  }

  private List<String> authorities(AuthClaimsProperties properties, Map<String, Object> claims) {
    return new JwtRolesGrantedAuthoritiesConverter(properties).convert(jwt(claims)).stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
  }

  @Test
  void mapsRolesAndCustomerGroupsWithKeycloakStyleClaims() {
    List<String> authorities = authorities(KEYCLOAK_STYLE, Map.of(
        "roles", List.of("Admin"),
        "groups", List.of("/Customers/ACME", "/Employees/Dev")));

    assertEquals(List.of("ROLE_ADMIN", "ROLE_CUSTOMER_ACME"), authorities);
  }

  @Test
  void mapsAppRolesWithEntraStyleClaims() {
    List<String> authorities = authorities(ENTRA_STYLE, Map.of(
        "roles", List.of("Admin", "Customer:ACME")));

    assertEquals(List.of("ROLE_ADMIN", "ROLE_CUSTOMER_ACME"), authorities);
  }

  @Test
  void ignoresBlankAndPrefixOnlyEntries() {
    List<String> authorities = authorities(KEYCLOAK_STYLE, Map.of(
        "roles", List.of(""),
        "groups", List.of("/Customers/")));

    assertTrue(authorities.isEmpty());
  }

  @Test
  void returnsNoAuthoritiesWhenClaimsAreMissing() {
    List<String> authorities = authorities(KEYCLOAK_STYLE, Map.of());

    assertTrue(authorities.isEmpty());
  }
}
