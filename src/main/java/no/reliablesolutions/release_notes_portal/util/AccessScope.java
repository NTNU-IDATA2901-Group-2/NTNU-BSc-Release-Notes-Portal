package no.reliablesolutions.release_notes_portal.util;

import java.util.List;

import lombok.Getter;

@Getter
public class AccessScope {
  private List<String> customerGroups;
  private boolean isAdmin;

  public AccessScope(List<String> customerGroups, boolean isAdmin) {
    this.customerGroups = customerGroups;
    this.isAdmin = isAdmin;
  }
}
