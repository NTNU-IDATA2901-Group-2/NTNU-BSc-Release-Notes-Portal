package no.reliablesolutions.release_notes_portal.service;

import java.util.List;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteHasNoGitCommitsException;
import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;
import no.reliablesolutions.release_notes_portal.domain.repository.PromptRepository;
import no.reliablesolutions.release_notes_portal.dto.PromptDTO;
import no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException;

@Service
@AllArgsConstructor
public class AiService {
    private final ChatClient.Builder builder;
    private final ChangeNoteService changeNoteService;
    private final DiffService diffService;
    private final GitRepositoryService gitRepositoryService;
    private final PromptRepository promptRepository;

    /**
     * <h1>Translates the given text to the specified locale using an AI model.</h1>
     * <h2>Supported Locales:</h2>
     * <ul>
     *   <li>en - English</li>
     *   <li>no - Norwegian Bokmål</li>
     *   <li>fr - French</li>
     * </ul>
     * 
     * @param locale the target locale for translation (e.g., "en", "no", "fr")
     * @param text the text to be translated
     * @return the translated text
     * @throws IllegalArgumentException if locale or text is null or empty
     * @throws LocaleNotSupportedException if the specified locale is not supported
     */
    public String translate(String locale, String text) {
        if (locale == null || locale.isEmpty() || text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Locale and text must not be null or empty");
        }

        String lang = "";
        switch (locale) {
            case "en" -> lang = "English";
            case "no" -> lang = "Norwegian Bokmål";
            case "fr" -> lang = "French";
            default -> throw new LocaleNotSupportedException(locale);
        }

        Prompt translationPrompt = promptRepository.findByName("Translation Prompt");

        if (translationPrompt == null) {
            throw new IllegalStateException("Translation prompt not found in database");
        }

        return builder.build().prompt().system("You are an assistant that should translate content. The language you should translate to is:  " + lang + ". " + translationPrompt.getPrompt())
                .user(text)
                .call()
                .content();
    }

    /**
     * Summarizes the change note with the given ID using an AI model. The summary is generated based on the git diff of the change note.
     * @param changeNoteId the ID of the change note to be summarized
     * @return a summary of the change note
     */
    public String summarizeChangeNote(long changeNoteId) {
      GitCommitHashAndPreviousGitCommitHash commits = changeNoteService.getGitCommitHashAndPreviousGitCommitHash(changeNoteId);
      if (commits == null) {
        throw new ChangeNoteHasNoGitCommitsException(changeNoteId);
      }
      GitRepository gitRepository = gitRepositoryService.getGitRepositoryForChangeNote(changeNoteId);
      String diffString = diffService.getDiffString(commits.getGitCommitHash(), commits.getPreviousGitCommitHash(), gitRepository);

      Prompt summarizeChangeNotePrompt = promptRepository.findByName("Change Note Summary");

      if (summarizeChangeNotePrompt == null) {
          throw new IllegalStateException("Summarize change note prompt not found in database");
      }

      return builder.build().prompt().system(summarizeChangeNotePrompt.getName())
                .user(diffString)
                .call()
                .content();
    }

    /**
     * Retrieves all prompts from the database and converts them to PromptDTOs.
     * @return a list of PromptDTOs representing the prompts stored in the database
     */
    public List<PromptDTO> getPrompts() {
        return promptRepository.findAllByOrderByNameAsc().stream().map(PromptDTO::fromPrompt).toList();
    }

    /**
     * Updates the prompts in the database based on the provided list of PromptDTOs.
     * Each PromptDTO should contain an ID that corresponds to an existing prompt in the database.
     * The method will update the name and prompt fields of each corresponding Prompt entity in the database.
     * @param prompts a list of PromptDTOs containing the updated prompt information
     */
    public void updatePrompts(List<PromptDTO> prompts) {
        List<Prompt> promptEntities = prompts.stream()
                .map(dto -> new Prompt(dto.id(), dto.name(), dto.prompt()))
                .toList();
        promptRepository.saveAll(promptEntities);
    }
}
