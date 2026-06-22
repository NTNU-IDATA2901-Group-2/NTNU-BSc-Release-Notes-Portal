package no.reliablesolutions.release_notes_portal.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.service.JiraService;

@RestController
@RequestMapping("/api/jira")
@AllArgsConstructor
@Profile("!ci")
public class JiraController {
  private JiraService jiraService;

  @GetMapping("/service-requests")
  public ResponseEntity<Map<String, String>> getServiceRequests(@RequestParam List<String> issueKeys) {
    return ResponseEntity.ok(jiraService.getServiceRequests(issueKeys));
  }

}
