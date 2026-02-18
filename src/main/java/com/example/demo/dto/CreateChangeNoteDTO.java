package com.example.demo.dto;

public record CreateChangeNoteDTO (
  String reference,
  String description,
  String developerNotes,
  String upgradeNotes,
  String changeSource,
  Long productId,
  Long scopeId,
  Long featureId,
  Long customerId,
  Boolean published
) {}
