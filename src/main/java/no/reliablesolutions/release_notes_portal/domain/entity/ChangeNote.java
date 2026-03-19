package no.reliablesolutions.release_notes_portal.domain.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
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
  private String changeSource = "";
  private final long timestamp = new Date().getTime();
  private boolean published = false;
  private boolean archived = false;

  @ManyToOne
  @JoinColumn(name = "release_note_id")
  private ReleaseNote releaseNote;

  @ManyToOne(optional = true)
  private Product product;

  @ManyToOne(optional = true)
  private Scope scope;

  @ManyToOne(optional = true)
  private Feature feature;

  @ManyToOne(optional = true)
  private Customer customer;
}
