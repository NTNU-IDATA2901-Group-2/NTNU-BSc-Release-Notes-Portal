package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.Scope;

public interface ScopeRepository extends JpaRepository<Scope, Long> {
  
}
