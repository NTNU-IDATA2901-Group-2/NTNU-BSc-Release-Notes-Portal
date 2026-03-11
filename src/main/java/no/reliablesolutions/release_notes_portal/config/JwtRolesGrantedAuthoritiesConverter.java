package no.reliablesolutions.release_notes_portal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;


/**
 * Custom converter to extract Keycloak realm roles from the JWT and convert them to Spring Security authorities
 */
public class JwtRolesGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private Logger logger = LoggerFactory.getLogger(JwtRolesGrantedAuthoritiesConverter.class);

  @Override
  public List<GrantedAuthority> convert(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess == null || !realmAccess.containsKey("roles")) {
      logger.warn("No 'realm_access.roles' claim found in JWT");
      return List.of();
    }

    ArrayList<String> roles = new ArrayList<>();
    Object rolesObj = realmAccess.get("roles");
    if (rolesObj instanceof List) {
      for (Object role : (List<?>) rolesObj) {
        if (role instanceof String string) {
          roles.add(string);
        } else {
          logger.warn("Unexpected type for role: {}", role.getClass().getName());
        }
      }
    } else {
      logger.warn("Unexpected type for 'roles' claim: {}", rolesObj.getClass().getName());
    }

    return roles.stream()
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
        .toList();
  }
}
