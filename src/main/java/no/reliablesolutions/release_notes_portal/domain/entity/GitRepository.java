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
    @NotBlank
    private String lastCheckedCommitHash;

    /**
     * Returns the local path for the Git repository based on the provided base directory.
     *
     * @param repositoryDirectoriesPath the base directory where Git repositories are stored
     * @return the local path for this Git repository
     */
    public String getLocalPath(String repositoryDirectoriesPath) {
        return repositoryDirectoriesPath + File.separator + this.getName();
    }
}


