package com.example.demo.domain.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ChangeNote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String reference;
  private String description;
  private String developerNotes;
  private String upgradeNotes;
  private final long timestamp = new Date().getTime();
  private boolean published = false;
  private boolean archived = false;

  private Product product;
  private Scope scope;
  private Feature feature;
  private Customer customer;
}
