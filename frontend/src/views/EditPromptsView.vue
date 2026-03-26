<script lang="ts" setup>
import { getPrompts, updatePrompts } from '@/api/ai-api';
import Button from '@/components/ui/button/Button.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import Textarea from '@/components/ui/textarea/Textarea.vue';
import { UpdatePromptsSchema } from '@/schemas';
import type { OnMutationApiCallFinished, Prompt } from '@/utils/types';
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query';
import { Save } from 'lucide-vue-next';
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue-sonner';

const { t } = useI18n();
const editablePrompts = ref<Prompt[] | undefined>(undefined);

const useGetPrompts = () => useQuery({
    queryKey: ['prompts'],
    queryFn: getPrompts,
    refetchOnMount: 'always',
})

const { isPending, isError, data: prompts } = useGetPrompts();

watch(prompts, (newPrompts) => {
    editablePrompts.value = newPrompts?.map((prompt) => ({ ...prompt }));
}, { immediate: true })


const useUpdatePrompts = (onFinished: OnMutationApiCallFinished) => {
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
            editablePrompts.value = prompts.value?.map((prompt) => ({ ...prompt }));
        },
    })
}


const savePromptsMutation = useUpdatePrompts({
    onSuccess: () => {
        toast.success(t('prompts.saveSuccess'));
    },
    onError: () => {
        toast.error(t('prompts.saveError'));
    }
});

const onSubmit = (event: Event) => {
    event.preventDefault();
    const parsedPrompts = UpdatePromptsSchema.safeParse({
        prompts: editablePrompts.value,
    });


    if (!parsedPrompts.success) {
        toast.error(t('prompts.saveError'));
        return;
    }

    savePromptsMutation.mutate({ prompts: parsedPrompts.data.prompts });
}
</script>

<template>
    <div class="flex flex-col items-center mt-20">
        <Spinner v-if="isPending" />
         <p v-else-if="isError">
             {{ t('prompts.errorLoadingPrompts') }}
         </p>
        <form v-if="editablePrompts" class="flex flex-col gap-8 w-full px-2 sm:w-[600px]" @submit="onSubmit">
            <h1 class="text-xl">
                {{ t('prompts.editPrompts') }}
            </h1>
            <div class="flex flex-col gap-2" v-for="prompt in editablePrompts" :key="prompt.id">
                <h2>
                    {{ prompt.name }}
                </h2>
                <Textarea :placeholder="t('prompts.promptPlaceholder')" class="w-full" v-model="prompt.prompt"></Textarea>
            </div>
            <Button variant="outline" type="submit">
                {{ t('button.save') }}
                <Save />
            </Button>
        </form>
    </div>
</template>