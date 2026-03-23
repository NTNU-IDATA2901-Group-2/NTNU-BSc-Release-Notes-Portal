package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.dto.CreateGitRepositoryDTO;
import no.reliablesolutions.release_notes_portal.service.GitRepositoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@Tag(name = "GitRepositories", description = "Endpoints for managing Git repositories")
@RestController
@RequestMapping("/api/git-repositories")
@AllArgsConstructor
public class GitRepositoryController {
    private final GitRepositoryService gitRepositoryService;
    private final Logger logger = LoggerFactory.getLogger(GitRepositoryController.class);

    @Operation(summary = "Create Git repository", description = "Creates a new Git repository with provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Git repository created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("")
    public ResponseEntity<Long> postMethodName(@RequestBody CreateGitRepositoryDTO entity) {
        long id = gitRepositoryService.createGitRepository(entity);
        logger.info("Git repository created with id: {}", id);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Delete Git repository", description = "Deletes an existing Git repository by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Git repository deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Git repository not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("")
    public ResponseEntity<String> deleteGitRepository(@RequestParam long id) {
        gitRepositoryService.deleteGitRepository(id);
        logger.info("Git repository deleted with id: {}", id);
        return ResponseEntity.ok("Git repository deleted successfully"); 
    }

    @Operation(summary = "Get all Git repositories", description = "Retrieves a list of all Git repositories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Git repositories retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<List<GitRepository>> getAllGitRepositories() {
        List<GitRepository> gitRepositories = gitRepositoryService.getAllGitRepositories();
        return ResponseEntity.ok(gitRepositories);
    }

    /**
     * Triggers synchronization of Git repositories.
     * @return a response entity indicating the result of the synchronization operation
     */
    @Operation(summary = "Sync Git repositories", description = "Syncs Git repositories with external source")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Git repositories synced successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/sync")
    public ResponseEntity<String> postMethodName() {
        gitRepositoryService.syncGitRepositories();
        logger.info("Git repositories synced successfully");
        return ResponseEntity.ok("Git repositories synced successfully");
    }

    /**
     * Triggers synchronization of a specific Git repository by ID.
     * @param id the ID of the Git repository to synchronize
     * @return a response entity indicating the result of the synchronization operation
     */
    @PostMapping("/sync/{id}")
    public ResponseEntity<String> postMethodName(@PathVariable long id) {
        gitRepositoryService.syncGitRepository(id);
        logger.info("Git repository with id {} synced successfully", id);
        return ResponseEntity.ok("Git repository synced successfully");
    }
    
    
}
