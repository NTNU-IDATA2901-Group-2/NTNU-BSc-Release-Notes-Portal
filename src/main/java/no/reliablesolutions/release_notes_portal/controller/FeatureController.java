package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.dto.CreateTagDTO;
import no.reliablesolutions.release_notes_portal.dto.FeatureDTO;
import no.reliablesolutions.release_notes_portal.service.FeatureService;

@Tag(name = "Features", description = "Endpoints for managing features")
@RestController
@RequestMapping("/api/features")
@AllArgsConstructor
public class FeatureController {

	private final FeatureService featureService;
	private final Logger logger = LoggerFactory.getLogger(FeatureController.class);

	@Operation(summary = "Create feature", description = "Creates a new feature with provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Feature created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request payload"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PostMapping("")
	public ResponseEntity<String> createFeature(@Valid @RequestBody CreateTagDTO featureDTO) {
		long id = featureService.createFeature(featureDTO);
		logger.info("Feature created with id: {}", id);
		return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(id));
	}

	@Operation(summary = "Get all features", description = "Retrieves a list of all features")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Features retrieved successfully"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("")
	public ResponseEntity<List<FeatureDTO>> getAllFeatures() {
		List<FeatureDTO> features = featureService.getAllFeatures();
		logger.info("Retrieved {} features", features.size());
		return ResponseEntity.ok(features);
	}

	@Operation(summary = "Get feature by ID", description = "Retrieves details of a specific feature by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Feature retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "Feature not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/{id}")
	public ResponseEntity<FeatureDTO> getFeatureById(@PathVariable long id) {
		FeatureDTO feature = featureService.getFeatureById(id);
		logger.info("Retrieved feature with id: {}", id);
		return ResponseEntity.ok(feature);
	}

	@Operation(summary = "Update feature", description = "Updates an existing feature with new details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Feature updated successfully"),
		@ApiResponse(responseCode = "404", description = "Feature not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PutMapping("/{id}")
	public ResponseEntity<FeatureDTO> updateFeature(@PathVariable long id, @Valid @RequestBody CreateTagDTO featureDTO) {
		FeatureDTO feature = featureService.updateFeature(id, featureDTO);
		logger.info("Updated feature with id: {}", id);
		return ResponseEntity.ok(feature);
	}

	@Operation(summary = "Delete feature", description = "Deletes an existing feature by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Feature deleted successfully"),
		@ApiResponse(responseCode = "404", description = "Feature not found"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteFeature(@PathVariable long id) {
		featureService.deleteFeature(id);
		logger.info("Deleted feature with id: {}", id);
		return ResponseEntity.ok("Feature deleted successfully");
	}
}
