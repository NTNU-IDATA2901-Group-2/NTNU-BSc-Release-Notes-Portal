<script setup lang="ts">
import type { ChangeNote, PersistChangeNoteDTO } from '@/utils/types';
import { Input } from '../ui/input';
import { Button } from '../ui/button';
import { Ban, Save, Sparkles } from 'lucide-vue-next';
import { Textarea } from '../ui/textarea';
import TagSelect from '../TagSelect.vue';
import { Separator } from '../ui/separator';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { EditChangeNoteSchema } from '@/schemas';
import { useGetHasCommits, useUpdateChangeNote } from '@/api/change-note-api';
import { toast } from 'vue-sonner';
import { router } from '@/utils/router';
import { useI18n } from 'vue-i18n';
import Checkbox from '../ui/checkbox/Checkbox.vue';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import DialogPrompt from '../DialogPrompt.vue';
import { useSummarizeChangeNotes } from '@/api/ai-api';
import Tooltip from '../ui/tooltip/Tooltip.vue';
import TooltipTrigger from '../ui/tooltip/TooltipTrigger.vue';
import TooltipContent from '../ui/tooltip/TooltipContent.vue';
import { onBeforeRouteLeave } from 'vue-router';
import { Spinner } from '../ui/spinner';

const props = defineProps<{
  changeNote: ChangeNote;
  modelValue?: boolean;
}>();

const showConfirmPrompt = ref(false);
const showConfirmCancelPrompt = ref(false);

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>();

const viewAbleByEveryone = ref(props.changeNote.viewableByEveryone);

const { handleSubmit, defineField } = useForm({
  validationSchema: toTypedSchema(EditChangeNoteSchema),
  initialValues: {
    title: props.changeNote.title ?? '',
    reference: props.changeNote.reference ?? '',
    description: props.changeNote.description ?? '',
    productId: props.changeNote.product?.id,
    scopeId: props.changeNote.scope?.id,
    featureId: props.changeNote.feature?.id,
    customerId: props.changeNote.customer?.id,
    developerNotes: props.changeNote.developerNotes ?? '',
    upgradeNotes: props.changeNote.upgradeNotes ?? '',
    viewableByEveryone: props.changeNote.viewableByEveryone ?? false,
}
});

const [title] = defineField('title');
const [reference] = defineField('reference');
const [description] = defineField('description');
const [productId] = defineField('productId');
const [scopeId] = defineField('scopeId');
const [featureId] = defineField('featureId');
const [customerId] = defineField('customerId');
const [developerNotes] = defineField('developerNotes');
const [upgradeNotes] = defineField('upgradeNotes');

const { t } = useI18n();

const updateChangeNoteMutation = useUpdateChangeNote({
    onSuccess: () => {
        toast.success(t('toast.changeNoteUpdatedSuccess'));
        emit('update:modelValue', false);
        router.push(`/change-notes/${props.changeNote.id}`);   
    },
    onError: () => {
        toast.error(t('toast.changeNoteUpdateError'));
    }
})

const onSubmit = handleSubmit((values : PersistChangeNoteDTO) => {
    values.productId = values.productId === -1 ? undefined : values.productId;
    values.scopeId = values.scopeId === -1 ? undefined : values.scopeId;
    values.featureId = values.featureId === -1 ? undefined : values.featureId;
    values.customerId = values.customerId === -1 ? undefined : values.customerId;
    values.viewableByEveryone = values.customerId === -1 ? undefined : viewAbleByEveryone.value;
    updateChangeNoteMutation.mutate({ id: props.changeNote.id.toString(), relatedReleaseNoteIds: props.changeNote.relatedReleaseNoteIds?.map(String), dto: values });
}, ({ errors }) => {
    console.error('Change note edit validation failed', errors);
    toast.error(t('toast.changeNoteUpdateError'));
});

const onCancel = () => {
    showConfirmCancelPrompt.value = true;
}

const cancelEdit = () => {
    emit('update:modelValue', false);
    router.push(`/change-notes/${props.changeNote.id}`);
}

const onViewableChecked = () => {
    if (!viewAbleByEveryone.value) {
        showConfirmPrompt.value = true;
    }
}

const onCancelViewable = () => {
    viewAbleByEveryone.value = false;
}

const loadingSummary = ref(false);

const summarizeChangeNote = useSummarizeChangeNotes({
    onSuccess: (summary?: string) => {
        description.value = summary;
        toast.success(t('toast.summarizeSuccess'));
    },
    onError: () => {
        toast.error(t('toast.summarizeError'));
    },
    onSettled: () => {
        loadingSummary.value = false;
    }
})

const onSummarize = () => {
    loadingSummary.value = true;
    summarizeChangeNote.mutate([props.changeNote.id]);
}

const hasCommits = useGetHasCommits([props.changeNote.id]);
const disableSummarizeButton = computed(() => hasCommits.isPending.value || hasCommits.isError.value || hasCommits.data.value !== true);  

// Warn user of unsaved changes when trying to leave the page
const beforeUnloadListener = (event: BeforeUnloadEvent) => {
     event.preventDefault();
    ;(event as unknown as { returnValue: string }).returnValue = ''
}

onBeforeRouteLeave(() => {
    return globalThis.confirm(t('changeNoteEdit.cancelDescription')) === true;
})

onMounted(() => {
    globalThis.addEventListener('beforeunload', beforeUnloadListener);
})

onBeforeUnmount(() => {
    globalThis.removeEventListener('beforeunload', beforeUnloadListener);
})
</script>

<template>
<DialogPrompt :open="showConfirmPrompt" :mode="'confirm'" :title-key="'changeNoteEdit.viewableByEveryoneTitle'" :description-key="'changeNoteEdit.viewableByEveryone'" @update:open="showConfirmPrompt = false" @cancel="onCancelViewable"/>
<DialogPrompt :open="showConfirmCancelPrompt" :mode="'confirm'" :title-key="'changeNoteEdit.cancelTitle'" :description-key="'changeNoteEdit.cancelDescription'" @update:open="showConfirmCancelPrompt = false" @confirm="cancelEdit"/>
<form @submit="onSubmit">
    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-16 lg:w-4xl md:mt-42">
    <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
        <div class="flex flex-col gap-1 w-full">
            <div class="flex sm:hidden ml-auto gap-4">
                <Button
                    :disabled="disableSummarizeButton || loadingSummary"
                    type="button"
                    @click="onSummarize"
                    variant="glow"
                >
                    {{t('button.summarize')}}
                    <Spinner size="sm" v-if="loadingSummary" class="h-4 dark:text-text-primary"/>
                    <Sparkles v-else/>
                </Button>
                <Button type="button" @click="onCancel" variant="outline">
                    {{ t('button.cancel') }}
                    <Ban />
                </Button>
                <Button type="submit" variant="outline">
                    {{ t('button.save') }}
                    <Save />
                </Button>
            </div>
            <h1 class="text-lg">{{ t('title.title') }}</h1>
            <div class="flex gap-4">
            <Input class="w-45" v-model="title" :placeholder="t('placeholder.title')" />
            <div class="hidden sm:flex ml-auto gap-4">
                <Tooltip>
                    <TooltipTrigger as-child>
                        <div class = "inline-flex">
                            <Button
                                :disabled="disableSummarizeButton  || loadingSummary"
                                type="button"
                                @click="onSummarize"
                                variant="glow"
                            >
                                {{t('button.summarize')}}
                                <Spinner v-if="loadingSummary" class="h-4 dark:text-text-primary" />
                                <Sparkles v-else />
                            </Button>
                        </div>
                    </TooltipTrigger>
                    <TooltipContent v-if="disableSummarizeButton">
                        {{ t('tooltip.noCommits') }}
                    </TooltipContent>
                    <TooltipContent v-else>
                        {{ t('tooltip.explainSummarize') }}
                    </TooltipContent>
                </Tooltip>
                <Button type="button" @click="onCancel" variant="outline">
                    {{ t('button.cancel') }}
                    <Ban />
                </Button>
                <Button type="submit" variant="outline">
                    {{ t('button.save') }}
                    <Save />
                </Button>
            </div>
        </div>
        </div>
        
        </div>

        <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.reference') }}</h1>
            <Input :placeholder="t('placeholder.reference')" class="w-45" v-model="reference"/>
        </div>

        <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.description') }}</h1>
            <Textarea :placeholder="t('placeholder.description')" class="w-full" v-model="description"></Textarea>
        </div>

        <div class="flex flex-wrap justify-between gap-4">
        <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.product') }}</h1>
            <TagSelect mode="product" v-model="productId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.scope') }}</h1>
            <TagSelect mode="scope" v-model="scopeId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.feature') }}</h1>
            <TagSelect mode="feature" v-model="featureId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.customer') }}</h1>
            <TagSelect mode="customer" v-model="customerId"/>
        </div>
        </div>
        <div v-if="customerId !== -1 && customerId !== undefined" class="flex flex-col gap-2">
            <h1 class="text-lg">{{ t('title.visibility') }}</h1>
            <Checkbox v-model="viewAbleByEveryone" @click="onViewableChecked"/>
        </div>
    </div>

    <Separator class="w-full h-2" />

    <div class="flex flex-col w-full gap-10">
        <div class="flex flex-col gap-1">
        <h1 class="text-lg">{{ t('title.developerNotes') }}</h1>
        <Textarea :placeholder="t('placeholder.developerNotes')" class="w-full" v-model="developerNotes"></Textarea>
        </div>
        <div class="flex flex-col gap-1">
        <h1 class="text-lg">{{ t('title.upgradeRequirements') }}</h1>
        <Textarea :placeholder="t('placeholder.upgradeRequirements')" class="w-full" v-model="upgradeNotes"></Textarea>
        </div>
    </div>
    </div>
</form>

</template>