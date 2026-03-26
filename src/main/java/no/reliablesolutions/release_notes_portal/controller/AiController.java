package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.dto.PromptDTO;
import no.reliablesolutions.release_notes_portal.service.AiService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

 

@RequestMapping("/api/ai")
@RestController
@AllArgsConstructor
public class AiController {
    private final Logger logger = LoggerFactory.getLogger(AiController.class);
    private final AiService aiService;


    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestParam(required = true) String locale, @RequestParam(required = true) String text) {
        logger.info("Translating text to locale: {}", locale);
        var response = aiService.translate(locale, text);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/prompts")
    public ResponseEntity<List<PromptDTO>> getAllPrompts() {
        logger.info("Getting all prompts");
        return ResponseEntity.ok(aiService.getPrompts());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/prompts")
    public ResponseEntity<Void> updatePrompts(@RequestBody List<PromptDTO> prompts) {
        logger.info("Updating prompts");
        aiService.updatePrompts(prompts);
        return ResponseEntity.ok().build();
    }
}