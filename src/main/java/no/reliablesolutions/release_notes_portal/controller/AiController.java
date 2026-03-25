package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.service.AiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
 

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
}