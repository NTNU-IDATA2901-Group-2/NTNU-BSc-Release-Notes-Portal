package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.service.AiService;

import org.springframework.http.ResponseEntity;
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


    /**
     * Translates the given text to the specified locale.
     * @param locale the target locale for the translation
     * @param text the text to be translated
     * @return a ResponseEntity containing the translated text
     */
    @Operation(summary = "Translate text", description = "Translates the given text to the specified locale using AI")
    @ApiResponses(value = {
        //TODO: Add responses
    })
    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestParam(required = true) String locale, @RequestParam(required = true) String text) {
        String response = aiService.translate(locale, text);
        return ResponseEntity.ok(response);
    }

    /**
     * Summarizes the change note with the given ID.
     * @param changeNoteId the ID of the change note to be summarized
     * @return a ResponseEntity containing the summary of the change note
     */
    @Operation(summary = "Summarize change note", description = "Generates a summary of the change note with the given ID using AI")
    @ApiResponses(value = {
        //TODO: Add responses
    })
    @GetMapping("/summarize-changenote/{changeNoteId}")
    public ResponseEntity<String> summarizeChangeNote(@PathVariable long changeNoteId) {
        String summary = aiService.summarizeChangeNote(changeNoteId);
        return ResponseEntity.ok(summary);
    }
}