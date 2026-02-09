package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
  
}
