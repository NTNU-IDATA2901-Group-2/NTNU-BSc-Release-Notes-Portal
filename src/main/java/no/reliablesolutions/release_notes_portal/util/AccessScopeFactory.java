package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

/**
 * A factory class for creating AccessScope instances based on the current user's authentication information.
 */
public class AccessScopeFactory {

  private AccessScopeFactory() {
    // Private constructor to prevent instantiation
  }

  /**
   * Creates an AccessScope instance based on the current user's authentication information.
   *
   * @return an AccessScope instance containing the user's customer groups and admin status
   */
  public static AccessScope fromCurrentUser() {
    List<String> customerGroups = AuthenticationUtil.getCustomerGroups();
    boolean isAdmin = AuthenticationUtil.isAdmin();

    return new AccessScope(customerGroups, isAdmin);
  }
}
