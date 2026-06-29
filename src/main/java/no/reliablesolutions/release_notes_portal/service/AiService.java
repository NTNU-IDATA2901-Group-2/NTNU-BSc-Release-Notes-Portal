package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;
import no.reliablesolutions.release_notes_portal.domain.repository.PromptRepository;
import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;
import no.reliablesolutions.release_notes_portal.dto.PromptDTO;
import no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException;
import no.reliablesolutions.release_notes_portal.util.SummarizeChangeNoteAgent;

/**
 * Service class for handling AI-related operations, such as translating text and summarizing change notes.
 */
@Service
@AllArgsConstructor
public class AiService {
    private final ChatClient.Builder builder;
    private final ChangeNoteService changeNoteService;
    private final ObjectProvider<DiffService> diffServiceProvider;
    private final GitRepositoryService gitRepositoryService;
    private final PromptRepository promptRepository;
    private final SummarizeChangeNoteAgent summarizeChangeNoteAgent;

    private final Logger logger = LoggerFactory.getLogger(AiService.class);
    
    /**
    * <h1>Translates the given text to the specified locale</h1>
    * <h2>Supported Locales:</h2>
    * <ul>
    *   <li>en-GB - English</li>
    *   <li>nb-NO - Norwegian Bokmål</li>
    *   <li>fr-FR - French</li>
    * </ul>
    * 
    * @param locale the target locale for translation (e.g., "en-GB", "nb-NO", "fr-FR")
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
            case "en-GB" -> lang = "English";
            case "nb-NO" -> lang = "Norwegian Bokmål";
            case "fr-FR" -> lang = "French";
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
    * Summarizes the change notes with the given IDs using an AI model. The summary is generated based on the git diff of the change notes.
    *
    * @param changeNoteIds the IDs of the change notes to be summarized
    * @return a string summarizing the contents of the change notes
    */
    public String summarizeChangeNote(List<Long> changeNoteIds) {
        StringBuilder diffs = new StringBuilder();
        DiffService diffService = diffServiceProvider.getIfAvailable();

        if (diffService == null) {
            logger.error("DiffService bean is not available. Cannot summarize change notes.");
            throw new IllegalStateException("DiffService is not available");
        }

        for (Long changeNoteId : changeNoteIds) {
            GitCommitHashAndPreviousGitCommitHash commits = changeNoteService.getGitCommitHashAndPreviousGitCommitHash(changeNoteId);
            if (commits == null || commits.getGitCommitHash() == null || commits.getPreviousGitCommitHash() == null) {
                logger.warn("Change note with ID {} is missing associated git commits. Skipping summarization for this change note.", changeNoteId);
                continue;
            }
            GitRepository gitRepository = gitRepositoryService.getGitRepositoryForChangeNote(changeNoteId);
            String diffString = diffService.getDiffString(commits.getGitCommitHash(), commits.getPreviousGitCommitHash(), gitRepository);
            diffs.append(diffString).append("\n");
        }
        String diffsString = diffs.toString().trim();
        
        Prompt summarizeChangeNotePrompt = promptRepository.findByName("Change Notes Summary");

        if (summarizeChangeNotePrompt == null) {
            throw new IllegalStateException("Summarize change note prompt not found in database");
        }
        
        return diffsString.isEmpty() ? "" : builder.build().prompt().system(summarizeChangeNotePrompt.getPrompt())
        .user(diffsString)
        .call()
        .content();
    }

    public String summarizeChangeNotesWithAgent(List<Long> changeNoteIds) {
        return summarizeChangeNoteAgent.summarizeChangeNotes(changeNoteIds);
    }

    /**
     * Retrieves all prompts from the database as PromptDTOs.
     *
     * @return a list of PromptDTOs representing the prompts stored in the database
     */
    public List<PromptDTO> getPrompts() {
        return promptRepository.findAllByOrderByNameAsc().stream().map(PromptDTO::fromPrompt).toList();
    }

    /**
     * Updates the prompts in the database based on the provided list of PromptDTOs.
     * Each PromptDTO should contain an ID that corresponds to an existing prompt in the database.
     *
     * @param prompts a list of PromptDTOs containing the updated prompt information
     */
    public void updatePrompts(List<PromptDTO> prompts) {
        List<Prompt> promptEntities = prompts.stream()
                .map(dto -> new Prompt(dto.id(), dto.name(), dto.prompt()))
                .toList();
        promptRepository.saveAll(promptEntities);
    }
}
