package no.reliablesolutions.release_notes_portal.service;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.dto.GitCommitHashAndPreviousGitCommitHash;
import no.reliablesolutions.release_notes_portal.exception.ChangeNoteHasNoGitCommitsException;
import no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException;

@Service
@AllArgsConstructor
public class AiService {
    private final ChatClient.Builder builder;
    private final ChangeNoteService changeNoteService;
    private final ReleaseNoteService releaseNoteService;
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
     * Summarizes the change note with the given ID using an AI model. The summary is generated based on the git diff of the change note.
     * @param changeNoteId the ID of the change note to be summarized
     * @return a summary of the change note
     */
    public String summarizeChangeNote(long changeNoteId) {
      GitCommitHashAndPreviousGitCommitHash commits = changeNoteService.getGitCommitHashAndPreviousGitCommitHash(changeNoteId);
      if (commits == null || commits.getGitCommitHash() == null || commits.getPreviousGitCommitHash() == null) {
        logger.warn("Change note with ID {} is missing associated git commits. Skipping summarization for this change note.", changeNoteId);
        return "";
      }
      GitRepository gitRepository = gitRepositoryService.getGitRepositoryForChangeNote(changeNoteId);
      String diffString = diffService.getDiffString(commits.getGitCommitHash(), commits.getPreviousGitCommitHash(), gitRepository);

      String masterPrompt = "You are working on a release note application. Summarize the git following git diff. Only highlight changes relevant for end users. Only talk about changes, no introduction. Do not mention anything that is not relevant for the end user when using the application. Only return the summary, without any explainations, additional information, comments, preamble or formatting.";

      return diffString.trim().isEmpty() ? "" : builder.build().prompt().system(masterPrompt)
                .user(diffString)
                .call()
                .content();
    }

    /**
     * Summarizes the release note with the given ID using an AI model. The summary is generated based on the change notes within the release note.
     * @param releaseNoteId the ID of the release note to be summarized
     * @return a summary of the release note
     */
    public String summarizeReleaseNote(long releaseNoteId) {
        StringBuilder releaseNoteDiffs = new StringBuilder();
        List<Long> changeNoteIds = changeNoteService.getChangeNotesIdsByReleaseNoteId(releaseNoteId);
        for (Long changeNoteId : changeNoteIds) {
            GitCommitHashAndPreviousGitCommitHash commits = changeNoteService.getGitCommitHashAndPreviousGitCommitHash(changeNoteId);
            if (commits == null || commits.getGitCommitHash() == null || commits.getPreviousGitCommitHash() == null) {
                logger.warn("Change note with ID {} is missing associated git commits. Skipping summarization for this change note.", changeNoteId);
                continue;
            }
            GitRepository gitRepository = gitRepositoryService.getGitRepositoryForChangeNote(changeNoteId);
            String diffString = diffService.getDiffString(commits.getGitCommitHash(), commits.getPreviousGitCommitHash(), gitRepository);
            releaseNoteDiffs.append(diffString).append("\n");
        }
        String masterPrompt = "You are working on a release note application. Summarize the git following git diffs for a release note. Only highlight changes relevant for end users. Only talk about changes, no introduction. Do not mention anything that is not relevant for the end user when using the application. Only return the summary, without any explainations, additional information, comments, preamble or formatting.";

        String releaseNoteDiffsString = releaseNoteDiffs.toString().trim();

        return releaseNoteDiffsString.isEmpty() ? "" : builder.build().prompt().system(masterPrompt)
                .user(releaseNoteDiffsString)
                .call()
                .content();
    }
}
