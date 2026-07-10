package no.reliablesolutions.release_notes_portal.domain.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a change note.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_git_commit_repository_file", columnNames = { "gitCommitHash",
    "gitRepository_id", "gitFilePath" }))
public class ChangeNote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String title = "";
  private String reference = "";
  @Column(columnDefinition = "TEXT")
  private String description = "";
  @Column(columnDefinition = "TEXT")
  private String developerNotes = "";
  @Column(columnDefinition = "TEXT")
  private String upgradeNotes = "";
  private final Instant creationTimestamp = Instant.now();
  private boolean published = false;
  private boolean archived = false;
  private boolean viewableByEveryone = false;

  @ManyToMany(mappedBy = "changeNotes")
  private List<ReleaseNote> releaseNotes;

  @ManyToOne(optional = true)
  private Product product;

  @ManyToOne(optional = true)
  private Scope scope;

  @ManyToOne(optional = true)
  private Feature feature;

  @ManyToOne(optional = true)
  private Customer customer;

  private String gitCommitHash;

  private String gitFilePath;

  private Instant gitCommitTimestamp;

  @ManyToOne(optional = true)
  private GitRepository gitRepository;


  /**
   * Adds a release note to the list of release notes associated with this change note.
   * 
   * @param releaseNote the release note to be added to this change note
   */
  public void addReleaseNote(ReleaseNote releaseNote) {
    this.releaseNotes.add(releaseNote);
  }

  /**
   * Removes a release note from the list of release notes associated with this change note.
   * @param releaseNote the release note to be removed from this change note
   */
  public void removeReleaseNote(ReleaseNote releaseNote) {
    this.releaseNotes.remove(releaseNote);
  }

  /**
   * Clears the list of release notes associated with this change note.
   */
  public void clearReleaseNotes() {
    this.releaseNotes.clear();
  }
}
