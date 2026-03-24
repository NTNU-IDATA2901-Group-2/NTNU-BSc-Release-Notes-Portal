package no.reliablesolutions.release_notes_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

  /**
   * Method for forwarding all non matched requests to the index.html, allowing the frontend router to handle the routing.
   * @return
   */
  @RequestMapping(value = "/{path:[^\\.]*}")
  public String redirect() {
    return "forward:/";
  }
}
