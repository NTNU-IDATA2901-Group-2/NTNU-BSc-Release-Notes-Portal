package no.reliablesolutions.release_notes_portal.domain.entity;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    /**
     * The directory within the Git repository where change note files are located.
     */
    @Setter
    private String changeNoteDirectory;

    /**
     * Personal access token used to authenticate against the remote. Never exposed through the API; clients only see {@link #isPatSet()}.
     */
    @Setter
    @JsonIgnore
    private String pat;

    @Setter
    private String lastCheckedCommitHash;

    /**
     * Returns whether a personal access token is configured for this repository.
     *
     * @return true if a personal access token is set, false otherwise
     */
    @JsonProperty("patSet")
    public boolean isPatSet() {
        return pat != null && !pat.isBlank();
    }

    /**
     * Returns the local path for the Git repository.
     *
     * @return the local path for this Git repository
     */
    public String getLocalPath() {
        return ChangeNotesSyncHandler.REPOSITORY_DIRECTORIES_PATH + File.separator + this.getName();
    }
}


