package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagDTO(
  @NotBlank()
  String name
) {}
