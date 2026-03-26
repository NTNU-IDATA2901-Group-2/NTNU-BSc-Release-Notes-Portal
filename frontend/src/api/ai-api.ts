import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";
import type { OnMutationApiCallFinished, Prompt } from "@/utils/types";

export interface TranslateInput {
    text: string;
    locale: string;
}

const translate = async (text: string, locale: string) => {
  const params = new URLSearchParams({ text, locale });
  const response = await api.post(`ai/translate`, params);
  return response.data;
}

/**
 * Custom hook for translating text using the AI translation API.
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @return A mutation object that can be used to trigger the translation process and manage its state.
 */
export const useTranslate = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();

        return useMutation<string, unknown, TranslateInput>({
                mutationFn: (input: TranslateInput) => translate(input.text, input.locale),
                onSuccess: (data: string) => {
            queryClient.invalidateQueries({ queryKey: ['translate'] });
                        onFinished.onSuccess(data);
        },
        onError: () => {
            console.error("Failed to translate text");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Retrieves all AI prompts from the backend API.
 * @returns a promise that resolves to an array of Prompt objects representing all AI prompts
 */
export const useGetPrompts = () => useQuery({
    queryKey: ['prompts'],
    queryFn: getPrompts,
    refetchOnMount: 'always',
})


/**
 * Updates the AI prompts by sending a PATCH request to the backend API with the provided list of Prompt objects. Each Prompt object should contain an ID that corresponds to an existing prompt in the database.
 * @param prompts a list of Prompt objects representing the prompts to be updated, where each Prompt object should contain an ID that corresponds to an existing prompt in the database
 * @returns a promise that resolves when the update operation is complete
 */
const updatePrompts = async (prompts: Prompt[]) => {
    const response = await api.patch(`ai/prompts`, prompts);
    return response.data;
}

/**
 * Custom hook for updating AI prompts by sending a PATCH request to the backend API with the provided list of Prompt objects. Each Prompt object should contain an ID that corresponds to an existing prompt in the database.
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @param onSettled An optional callback function to be called when the mutation is settled, regardless of success or error.
 * @returns a mutation object that can be used to trigger the update process and manage its state.
 */
export const useUpdatePrompts = (onFinished: OnMutationApiCallFinished, onSettled?: () => void) => {
    const queryClient = useQueryClient();

        return useMutation<string, unknown, { prompts: Prompt[] }>({
                mutationFn: (input) => updatePrompts(input.prompts),
                onSuccess: (data: string) => {
            queryClient.invalidateQueries({ queryKey: ['prompts'] });
                        onFinished.onSuccess(data);
        },
        onError: () => {
            console.error("Failed to update prompts");
            onFinished.onError();
        },
        onSettled: () => {
            onSettled?.();
        },
    })
}

/**
 * Retrieves all AI prompts from the backend API.
 * @returns a promise that resolves to an array of Prompt objects representing all AI prompts
 */
export const getPrompts = async () => {
    const response = await api.get(`ai/prompts`);
    return response.data as Prompt[];
}

/**
 * Summarizes a change note using the AI summarization API.
 * @param changeNoteId the ID of the change note to be summarized
 * @returns a promise that resolves to the summarized text of the change note
 */
const summarizeChangeNote = async (changeNoteId: number) => {
  const response = await api.get(`ai/summarize-changenote/${changeNoteId}`);
  return response.data;
}

/**
 * Custom hook for summarizing a change note using the AI summarization API.
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @returns A mutation object that can be used to trigger the summarization process.
 */
export const useSummarizeChangeNote = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (changeNoteId: number) => summarizeChangeNote(changeNoteId),
        onSuccess: (data: string) => {
            queryClient.invalidateQueries({ queryKey: ['summarizeChangeNote'] });
            onFinished.onSuccess(data);
        },
        onError: () => {
            console.error("Failed to summarize change note");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}