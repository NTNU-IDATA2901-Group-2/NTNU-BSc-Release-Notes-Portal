package com.example.demo.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.ChangeNote;

public interface ChangeNoteRepository extends JpaRepository<ChangeNote, Long> {
  
}
