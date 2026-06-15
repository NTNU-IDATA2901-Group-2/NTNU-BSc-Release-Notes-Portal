package no.reliablesolutions.release_notes_portal.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing the impact a change has on a feature, describing what changed,
 * what should be tested and how urgently it should be tested.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ChangeImpact {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = true)
  private Feature feature;

  @Column(columnDefinition = "TEXT")
  private String whatIsChanged = "";

  @Column(columnDefinition = "TEXT")
  private String whatShouldBeTested = "";

  @Enumerated(EnumType.STRING)
  private TestingNeed testingNeed;

  /**
   * Degree to which a change impact needs to be tested.
   */
  public enum TestingNeed {
    LOW,
    LOW_MEDIUM,
    MEDIUM,
    MEDIUM_HIGH,
    HIGH
  }
}
