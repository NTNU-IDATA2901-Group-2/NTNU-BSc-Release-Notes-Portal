package no.reliablesolutions.release_notes_portal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom converter for mapping jwt roles and customer entries to Spring
 * Security granted authorities. The claim names and customer prefix are
 * provided by {@link AuthClaimsProperties} so any OIDC provider can be used.
 */
public class JwtRolesGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  private static final String CUSTOMER_PREFIX = "CUSTOMER_";
  private static final String ROLE_PREFIX = "ROLE_";

  private final AuthClaimsProperties claims;

  public JwtRolesGrantedAuthoritiesConverter(AuthClaimsProperties claims) {
    this.claims = claims;
  }

  @Override
  public List<GrantedAuthority> convert(Jwt jwt) {
    ArrayList<String> roles = new ArrayList<>();
    List<String> roleStrings = jwt.getClaimAsStringList(claims.rolesClaim());
    if (roleStrings != null) {
      roleStrings.stream()
          .filter(role -> !role.isBlank())
          // Customer entries may share the roles claim (e.g. Entra app roles)
          .filter(role -> !role.startsWith(claims.customerPrefix()))
          .forEach(role -> roles.add(role.toUpperCase()));
    }

    List<String> groups = jwt.getClaimAsStringList(claims.customerClaim());
    if (groups != null) {
      groups.stream()
          .filter(group -> group.startsWith(claims.customerPrefix()))
          .map(group -> group.substring(claims.customerPrefix().length()))
          .filter(group -> !group.isBlank())
          .forEach(group -> roles.add(CUSTOMER_PREFIX + group.toUpperCase()));
    }

    return roles.stream()
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
        .toList();
  }
}
