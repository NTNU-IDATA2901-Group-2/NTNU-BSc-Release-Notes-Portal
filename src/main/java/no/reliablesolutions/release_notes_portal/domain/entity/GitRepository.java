package no.reliablesolutions.release_notes_portal.domain.entity;

import java.io.File;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.reliablesolutions.release_notes_portal.runner.ChangeNotesSyncHandler;

/**
 * Entity representing a Git repository.
 */
@Entity
@NoArgsConstructor
@Getter
public class GitRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @NotBlank
    private String name;

    @Setter
    @NotBlank
    private String url;

    @Setter
    private String lastCheckedCommitHash;

    /**
     * Returns the local path for the Git repository.
     *
     * @return the local path for this Git repository
     */
    public String getLocalPath() {
        return ChangeNotesSyncHandler.REPOSITORY_DIRECTORIES_PATH + File.separator + this.getName();
    }
}


