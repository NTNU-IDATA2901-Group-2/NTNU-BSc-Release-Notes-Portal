package no.reliablesolutions.release_notes_portal.domain.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
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

  @Embedded
  private ReleaseTimeline releaseTimeline;

  @ElementCollection
  @OrderColumn(name = "position")
  @Column(name = "limitation", columnDefinition = "TEXT")
  private List<String> knownLimitations = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "release_note_id")
  @OrderColumn(name = "position")
  private List<ChangeImpact> changeImpacts = new ArrayList<>();
}
