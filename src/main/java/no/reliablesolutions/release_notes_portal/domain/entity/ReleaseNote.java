package no.reliablesolutions.release_notes_portal.domain.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class for a release note.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ReleaseNote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToMany
  @JoinTable(
    name = "release_note_change_note",
    joinColumns = @JoinColumn(name = "release_note_id"),
    inverseJoinColumns = @JoinColumn(name = "change_note_id")
  )
  private List<ChangeNote> changeNotes;

  private String tag = "";
  @Column(columnDefinition = "TEXT")
  private String summary = "";
  private Boolean published = false;
  private Boolean archived = false;
  private final Instant createdAt = Instant.now();
  private Boolean syncedToGit = false;
}
