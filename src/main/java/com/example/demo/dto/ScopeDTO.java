package com.example.demo.dto;

import com.example.demo.domain.entity.Scope;

public record ScopeDTO(
  long id,
  String name
) {
  public static ScopeDTO fromScope(Scope scope) {
    return new ScopeDTO(scope.getId(), scope.getName());
  }
}
