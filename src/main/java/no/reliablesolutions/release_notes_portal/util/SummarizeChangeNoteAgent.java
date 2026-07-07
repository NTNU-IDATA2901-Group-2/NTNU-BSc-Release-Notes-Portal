package no.reliablesolutions.release_notes_portal.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import no.reliablesolutions.release_notes_portal.domain.repository.PromptRepository;
import no.reliablesolutions.release_notes_portal.service.ChangeNoteService;
import no.reliablesolutions.release_notes_portal.service.GitRepositoryService;
import no.reliablesolutions.release_notes_portal.service.ChangeNoteGitInspectionService;

@Component
@Profile("!ci")
public class SummarizeChangeNoteAgent {
  private final ChatClient chatClient;
  private final ChangeNoteService changeNoteService;
  private final GitRepositoryService gitRepositoryService;
  private final ChangeNoteGitInspectionService changeNoteGitInspectionService;
  private final PromptRepository promptRepository;

  public SummarizeChangeNoteAgent(ChatClient.Builder chatClientBuilder, ChangeNoteService changeNoteService,
      GitRepositoryService gitRepositoryService, ChangeNoteGitInspectionService changeNoteGitInspectionService, PromptRepository promptRepository) {
    this.chatClient = chatClientBuilder.build();
    this.changeNoteService = changeNoteService;
    this.gitRepositoryService = gitRepositoryService;
    this.changeNoteGitInspectionService = changeNoteGitInspectionService;
    this.promptRepository = promptRepository;
  }  

  public String summarizeChangeNotes(List<Long> changeNoteIds) {
    Prompt prompt = buildPrompt(changeNoteIds);
    AgentResponse response = chatClient.prompt(prompt).toolCallbacks(
      MethodToolCallbackProvider.builder().toolObjects(
        changeNoteService,
        gitRepositoryService,
        changeNoteGitInspectionService
      ).build()
    )
    .call()
    .entity(AgentResponse.class);

    return response.answer();
  }

  private Prompt buildPrompt(List<Long> changeNoteIds) {
        List<Message> messages = new ArrayList<>();
            no.reliablesolutions.release_notes_portal.domain.entity.Prompt summarizeChangeNotePrompt = promptRepository.findByName("Change Notes Summary");
    messages.add(new SystemMessage(String.format(summarizeChangeNotePrompt.getPrompt() + "Change note IDs: %s", changeNoteIds)));

    return new Prompt(messages);

  }

  private record AgentResponse(String answer, String title) {
  }
}
