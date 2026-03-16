package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import no.reliablesolutions.release_notes_portal.util.AiWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
 

@RequestMapping("/api/ai")
@RestController
@AllArgsConstructor
public class AiController {
    private final AiWrapper ai;
    private final Logger logger = LoggerFactory.getLogger(AiController.class);

    @PostMapping("")
    public ResponseEntity<String> postMethodName(@RequestParam String prompt) {
        logger.info("Received AI request with prompt: {}", prompt);
        return ResponseEntity.ok(ai.getResponse(prompt));
    }
}