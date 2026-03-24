package no.reliablesolutions.release_notes_portal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom converter to extract Keycloak realm roles from the JWT and convert
 * them to Spring Security authorities
 */
public class JwtRolesGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  private static final String ROLES_CLAIM = "roles";
  private static final String GROUPS_CLAIM = "groups";
  private static final String CUSTOMER_GROUP_PREFIX = "/Customers/";
  private static final String CUSTOMER_PREFIX = "CUSTOMER_";
  private static final String ROLE_PREFIX = "ROLE_";

  @Override
  public List<GrantedAuthority> convert(Jwt jwt) {
    ArrayList<String> roles = new ArrayList<>();
    List<String> roleStrings = jwt.getClaimAsStringList(ROLES_CLAIM);
    if (roleStrings != null) {
      roleStrings.stream()
          .filter(role -> !role.isBlank())
          .forEach(role -> roles.add(role.toUpperCase()));
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
