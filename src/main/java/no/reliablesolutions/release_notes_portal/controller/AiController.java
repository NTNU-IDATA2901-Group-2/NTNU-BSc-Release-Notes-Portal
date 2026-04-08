package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.service.AiService;

import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

 

@RequestMapping("/api/ai")
@RestController
@AllArgsConstructor
public class AiController {
    private final AiService aiService;
    private final Logger logger = LoggerFactory.getLogger(AiController.class);

    /**
     * Translates the given text to the specified locale.
     * @param locale the target locale for the translation
     * @param text the text to be translated
     * @return a ResponseEntity containing the translated text
     */
    @Operation(summary = "Translate text", description = "Translates the given text to the specified locale using AI")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Text translated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestParam(required = true) String locale, @RequestParam(required = true) String text) {
        logger.info("Translating text to locale: {}", locale);
        String response = aiService.translate(locale, text);
        return ResponseEntity.ok(response);
    }

    /**
     * Summarizes the change notes with the given IDs.
     * @param changeNoteIds the IDs of the change notes to be summarized
     * @return a ResponseEntity containing the summary of the change notes
     */
    @Operation(summary = "Summarize change notes", description = "Generates a summary of the change notes with the given IDs using AI")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summarize-changenotes/{changeNoteIds}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> summarizeChangeNote(@PathVariable List<Long> changeNoteIds) {
        String summary = aiService.summarizeChangeNote(changeNoteIds);
        return ResponseEntity.ok(summary);
    }

}