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

/**
 * Summarizes a release note using the AI summarization API.
 * @param releaseNoteId the ID of the release note to be summarized
 * @returns a promise that resolves to the summarized text of the release note
 */
const summarizeReleaseNote = async (releaseNoteId: number) => {
  const response = await api.get(`ai/summarize-releasenote/${releaseNoteId}`);
  return response.data;
}

/**
 * Custom hook for summarizing a release note using the AI summarization API.
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @returns A mutation object that can be used to trigger the summarization process.
 */
export const useSummarizeReleaseNote = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (releaseNoteId: number) => summarizeReleaseNote(releaseNoteId),
        onSuccess: (data: string) => {
            queryClient.invalidateQueries({ queryKey: ['summarizeReleaseNote'] });
            onFinished.onSuccess(data);
        },
        onError: () => {
            console.error("Failed to summarize release note");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}