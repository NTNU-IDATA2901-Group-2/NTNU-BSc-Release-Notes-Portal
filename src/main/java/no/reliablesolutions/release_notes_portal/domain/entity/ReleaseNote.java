package no.reliablesolutions.release_notes_portal.domain.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

  @OneToMany(mappedBy = "releaseNote")
  private List<ChangeNote> changeNotes;

  private String tag = "";
  @Column(columnDefinition = "TEXT")
  private String summary = "";
  private Boolean published = false;
  private Boolean archived = false;
  private final Long createdAt = new Date().getTime();
}
