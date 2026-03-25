package no.reliablesolutions.release_notes_portal.domain.entity;

import java.util.Date;
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

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
  uniqueConstraints = @UniqueConstraint(name="unique_git_commit_repository", columnNames = {"gitCommitHash", "gitRepository_id"})
)
public class ChangeNote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String reference = "";
  @Column(columnDefinition = "TEXT")
  private String description = "";
  @Column(columnDefinition = "TEXT")
  private String developerNotes = "";
  @Column(columnDefinition = "TEXT")
  private String upgradeNotes = "";
  private final long creationTimestamp = new Date().getTime();
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

  private Long gitCommitTimestamp;

  @ManyToOne(optional = true)
  private GitRepository gitRepository;
}
