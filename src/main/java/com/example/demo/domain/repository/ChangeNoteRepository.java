package com.example.demo.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.ChangeNote;

public interface ChangeNoteRepository extends JpaRepository<ChangeNote, Long> {
  List<ChangeNote> findByArchivedFalse();
  Optional<ChangeNote> findByIdAndArchivedFalse(Long id);
}
