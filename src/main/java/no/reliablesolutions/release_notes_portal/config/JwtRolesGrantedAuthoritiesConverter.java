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
 * Custom converter to extract Keycloak realm roles from the JWT and convert
 * them to Spring Security authorities
 */
public class JwtRolesGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private static final String REALM_ACCESS = "realm_access";
  private static final String ROLES = "roles";
  private static final String GROUPS_CLAIM = "groups";
  private static final String CUSTOMER_GROUP_PREFIX = "/Customers/";
  private static final String CUSTOMER_PREFIX = "CUSTOMER_";
  private static final String ROLE_PREFIX = "ROLE_";

  private Logger logger = LoggerFactory.getLogger(JwtRolesGrantedAuthoritiesConverter.class);

  @Override
  public List<GrantedAuthority> convert(Jwt jwt) {
    ArrayList<String> roles = new ArrayList<>();
    Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
    if (realmAccess != null && realmAccess.containsKey(ROLES)) {
      Object rolesObj = realmAccess.get(ROLES);
      if (rolesObj instanceof List) {
        for (Object role : (List<?>) rolesObj) {
          if (role instanceof String string) {
            roles.add(string.toUpperCase());
          } else {
            logger.warn("Unexpected type for role: {}", role.getClass().getName());
          }
        }
      } else {
        logger.warn("Unexpected type for 'roles' claim: {}", rolesObj.getClass().getName());
      }
    }


    List<String> groups = jwt.getClaimAsStringList(GROUPS_CLAIM);
    if (groups != null) {
      groups.stream()
        .filter(group -> group.startsWith(CUSTOMER_GROUP_PREFIX))
        .map(group -> group.substring(CUSTOMER_GROUP_PREFIX.length()))
        .filter(group -> !group.isBlank())
        .forEach(group -> roles.add(CUSTOMER_PREFIX + group.toUpperCase()));
    }
    
    return roles.stream()
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
        .toList();
  }



}
