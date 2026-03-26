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

    public String getLocalPath(String repositoryDirectoriesPath) {
        return repositoryDirectoriesPath + File.separator + this.getName();
    }
}


