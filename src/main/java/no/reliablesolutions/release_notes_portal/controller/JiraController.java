package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.service.JiraService;

@RestController
@RequestMapping("/api/jira")
@AllArgsConstructor
public class JiraController {
private JiraService jiraService;
  @GetMapping("/service-request/{issueKey}")
  public ResponseEntity<String> getServiceRequest(@PathVariable String issueKey) {
    String sr = jiraService.getServiceRequest(issueKey);
    return ResponseEntity.ok(sr);
  }

}
