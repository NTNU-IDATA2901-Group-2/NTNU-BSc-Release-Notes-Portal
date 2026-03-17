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