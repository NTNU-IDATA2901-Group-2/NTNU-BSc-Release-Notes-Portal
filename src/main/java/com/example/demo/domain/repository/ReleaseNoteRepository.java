package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.ReleaseNote;

public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Long> {
  
}
