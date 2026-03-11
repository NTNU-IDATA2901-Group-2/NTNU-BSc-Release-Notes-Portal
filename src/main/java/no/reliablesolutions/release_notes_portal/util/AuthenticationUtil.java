package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtil {
  static final String ROLE_CUSTOMER_PREFIX = "ROLE_CUSTOMER_";
  static final String ROLE_ADMIN = "ROLE_ADMIN";

  private AuthenticationUtil() {
    // Private constructor to prevent instantiation
  }

  public static boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
  }

  public static List<String> getCustomerGroups() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      return List.of();
    }

    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith(ROLE_CUSTOMER_PREFIX))
        .map(authority -> authority.substring(ROLE_CUSTOMER_PREFIX.length()))
        .toList();
  }
}
