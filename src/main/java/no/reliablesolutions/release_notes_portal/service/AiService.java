package no.reliablesolutions.release_notes_portal.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.Prompt;
import no.reliablesolutions.release_notes_portal.domain.repository.PromptRepository;
import no.reliablesolutions.release_notes_portal.dto.PromptDTO;
import no.reliablesolutions.release_notes_portal.exception.LocaleNotSupportedException;

@Service
@AllArgsConstructor
public class AiService {
    private final ChatClient.Builder builder;
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
            case "en":
                lang = "English";
                break;

            case "no":
                lang = "Norwegian Bokmål";
                break;

            case "fr":
                lang = "French";
                break;

            default:
                throw new LocaleNotSupportedException(locale);
        }

        String masterPrompt = "You are an assistant that is part of a release notes portal. You are helping users translate the content of release and change notes to their preferred language. The user has requested a translation to " + lang + ". Please translate the following text to " + lang + ". Make sure to maintain the original meaning and context of the text, and ensure that the translation is accurate and natural-sounding in " + lang + ". Make sure to avoid grammatical errors and awkward phrasing. Only return the translated text, without any explainations, additional information, comments, preamble or formatting. If the provided text is given in markdown it is expected to be returned in markdown.";

        return builder.build().prompt().system(masterPrompt)
                .user(text)
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
