package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

public class AccessScopeFactory {

  private AccessScopeFactory() {
    // Private constructor to prevent instantiation
  }

  public static AccessScope fromCurrentUser() {
    List<String> customerGroups = AuthenticationUtil.getCustomerGroups();
    boolean isAdmin = AuthenticationUtil.isAdmin();

    return new AccessScope(customerGroups, isAdmin);
  }
}
