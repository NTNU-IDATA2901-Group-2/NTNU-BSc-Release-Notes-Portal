package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for retrieving authentication information about the current user, such as their customer groups and admin status.
 */
public class AuthenticationUtil {
  static final String ROLE_CUSTOMER_PREFIX = "ROLE_CUSTOMER_";
  static final String ROLE_ADMIN = "ROLE_ADMIN";

  private AuthenticationUtil() {
    // Private constructor to prevent instantiation
  }

  /**
   * Checks if the current user has admin privileges.
   *
   * @return true if the current user is an admin, false otherwise
   */
  public static boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
  }

  /**
   * Retrieves the list of customer groups that the current user belongs to.
   *
   * @return a list of customer group names that the current user belongs to
   */
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
