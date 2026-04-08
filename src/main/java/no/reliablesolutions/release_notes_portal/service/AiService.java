package no.reliablesolutions.release_notes_portal.service;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;
import no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException;

@Service
@AllArgsConstructor
public class AiService {
    private final ChatClient.Builder builder;
    private final ChangeNoteService changeNoteService;
    private final DiffService diffService;
    private final GitRepositoryService gitRepositoryService;
    private final Logger logger = LoggerFactory.getLogger(AiService.class);
    
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
        
        String masterPrompt = "You are an assistant that is part of a release notes portal. You are helping users translate the content of release and change notes to their preferred language. The user has requested a translation to " + lang + ". Please translate the following text to " + lang + ". Make sure to maintain the original meaning and context of the text, and ensure that the translation is accurate and natural-sounding in " + lang + ". Make sure to avoid grammatical errors and awkward phrasing. Only return the translated text, without any explainations, additional information, comments, preamble or formatting. If the provided text is given in markdown it is expected to be returned in markdown.";
        
        return builder.build().prompt().system(masterPrompt)
        .user(text)
        .call()
        .content();
    }
    
    /**
    * Summarizes the change notes with the given IDs using an AI model. The summary is generated based on the git diff of the change notes.
    * @param changeNoteIds the IDs of the change notes to be summarized
    * @return a summary of the change notes
    */
    public String summarizeChangeNote(List<Long> changeNoteIds) {
        StringBuilder diffs = new StringBuilder();
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
        
        String masterPrompt = """
                You are an assistant for a release notes portal. Summarize the provided git diff(s) into a concise free text summary for end users. 
                Include only user-facing changes (features, fixes, UI/UX changes, behavior changes). 
                Exclude internal/refactoring/dev tooling/test/build/formatting changes unless they affect user behavior. 
                Do not invent details. If information is unclear, omit it. Output only the summary as free text. 
                No introduction, no conclusion, no headings, no extra commentary. Make no mistakes.
                """;
        
        return diffsString.isEmpty() ? "" : builder.build().prompt().system(masterPrompt)
        .user(diffsString)
        .call()
        .content();
    }
}
