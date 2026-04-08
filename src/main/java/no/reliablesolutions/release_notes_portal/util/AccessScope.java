package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import lombok.Getter;

/**
 * A class representing the access scope of a user, including their customer groups and whether they are an admin.
 */
@Getter
public class AccessScope {
  private List<String> customerGroups;
  private boolean isAdmin;

  /**
   * Constructs a new AccessScope with the specified customer groups and admin status.
   *
   * @param customerGroups the list of customer groups the user belongs to
   * @param isAdmin whether the user has admin privileges
   */
  public AccessScope(List<String> customerGroups, boolean isAdmin) {
    this.customerGroups = customerGroups;
    this.isAdmin = isAdmin;
  }
}
