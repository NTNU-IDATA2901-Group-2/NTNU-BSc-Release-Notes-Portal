import { useMutation, useQueryClient } from "@tanstack/vue-query";
import api from "./api";
import type { OnMutationApiCallFinished } from "@/utils/types";

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
 * Summarizes change notes using the AI summarization API.
 * @param changeNoteIds an array of IDs of the change notes to be summarized
 * @returns a promise that resolves to the summarized text of the change notes
 */
const summarizeChangeNotes = async (changeNoteIds: number[]) => {
  const response = await api.get(`ai/summarize-changenotes/${changeNoteIds.join(',')}`);
  return response.data;
}

/**
 * Custom hook for summarizing change notes using the AI summarization API.
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @returns A mutation object that can be used to trigger the summarization process.
 */
export const useSummarizeChangeNotes = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (changeNoteIds: number[]) => summarizeChangeNotes(changeNoteIds),
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