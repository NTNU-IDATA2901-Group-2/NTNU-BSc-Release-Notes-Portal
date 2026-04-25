package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for forwarding all non-matched requests to the index.html
 * 
 * This is done to allow the frontend router to handle the routing for the single page application (SPA).
 */
@Controller
public class SpaForwardController {

  /**
   * Method for forwarding all non matched requests to the index.html, allowing the frontend router to handle the routing.
   * @return returning a forward to the index.html
   */
  @RequestMapping(value = "/{path:[^\\.]*}")
  public String redirect() {
    return "forward:/";
  }
}
