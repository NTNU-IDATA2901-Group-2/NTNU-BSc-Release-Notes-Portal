package no.reliablesolutions.release_notes_portal.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
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

  @ManyToOne(optional = false)
  private Feature feature;

  @Column(columnDefinition = "TEXT")
  @NotNull
  private String whatIsChanged;

  @Column(columnDefinition = "TEXT")
  @NotNull
  private String whatShouldBeTested;

  @Enumerated(EnumType.STRING)
  @NotNull
  private TestingNeed testingNeed;

  /**
   * Creates a change impact for a feature. The {@code id} is assigned by the
   * persistence provider on save.
   */
  public ChangeImpact(Feature feature, String whatIsChanged, String whatShouldBeTested, TestingNeed testingNeed) {
    this.feature = feature;
    this.whatIsChanged = whatIsChanged;
    this.whatShouldBeTested = whatShouldBeTested;
    this.testingNeed = testingNeed;
  }

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
