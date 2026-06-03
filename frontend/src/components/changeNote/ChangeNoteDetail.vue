<script lang="ts" setup>
import type { ChangeNote } from '@/utils/types';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Check, Copy, EllipsisVertical, Eye, Pencil, Sparkles, Trash2 } from 'lucide-vue-next';
import DialogPrompt from '../DialogPrompt.vue';
import { computed, onBeforeUnmount, ref } from 'vue';
import { useArchiveChangeNote, usePublishChangeNote } from '@/api/change-note-api';
import { toast } from 'vue-sonner';
import { router } from '@/utils/router';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import md from '@/utils/markdown-it';
import { useTranslate } from '@/api/ai-api';
import Button from '../ui/button/Button.vue';
import Spinner from '../ui/spinner/Spinner.vue';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { openJiraTicket } from '@/utils/jira.ts';

const { t, locale } = useI18n();

const props = defineProps<{
    changeNote: ChangeNote;
    modelValue?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>();

const showDeletePrompt = ref(false);
const translatedDescription = ref<string | null>(null);
const isTranslating = ref(false);

const deleteChangeNoteMutation = useArchiveChangeNote(props.changeNote.id, {
  onSuccess: () => {
    toast.success(t('toast.changeNoteDeleted'));
    router.push("/change-notes");
  },
  onError: () => {
    toast.error(t('toast.changeNoteDeleteError'));
  },
});

const onDelete = () => {
  deleteChangeNoteMutation.mutate();
  emit('update:modelValue', false);
}


const { mutate: publishChangeNoteMutation } = usePublishChangeNote(props.changeNote.id, !props.changeNote.published, {
  onSuccess: () => {
    toast.success(`${props.changeNote.published ? t('toast.changeNoteUnpublished') : t('toast.changeNotePublished')}`);
  },
  onError: () => {
    toast.error(`${props.changeNote.published ? t('toast.changeNoteUnpublishError') : t('toast.changeNotePublishError')}`);
  },
});

const onPublishToggle = () => {
  publishChangeNoteMutation(!props.changeNote.published);
}

const hasTranslation = computed(() => translatedDescription.value !== null);

const translateMutation = useTranslate({
  onSuccess: () => {
    toast.success(t('toast.translationSuccess'));
  },
  onError: () => {
    toast.error(t('toast.translationError'));
  },
});

const onTranslate = async () => {
  if (translatedDescription.value) {
    translatedDescription.value = null;
    return;
  }
  isTranslating.value = true;
  const result = await translateMutation.mutateAsync({
    text: props.changeNote.description,
    locale: locale.value,
  });
  translatedDescription.value = result;
  isTranslating.value = false;
}

const copiedKey = ref<string | null>(null);
let copyResetTimeout: ReturnType<typeof setTimeout> | null = null;

const resetCopiedState = () => {
  copiedKey.value = null;
  if (copyResetTimeout) {
    clearTimeout(copyResetTimeout);
    copyResetTimeout = null;
  }
};

const handleCopy = (text: string | null | undefined, key?: string) => {
  if (!props.changeNote) return;

  navigator.clipboard.writeText(text ?? '')
    .then(() => {
      if (key) {
      copiedKey.value = key;
      if (copyResetTimeout) {
        clearTimeout(copyResetTimeout);
      }
      copyResetTimeout = setTimeout(() => {
        copiedKey.value = null;
        copyResetTimeout = null;
      }, 3000);
    }
      toast.success(t('toast.copySuccess'));
    })
    .catch((err) => {
      console.error('Error copying to clipboard:', err);
      toast.error(t('toast.copyError'));
    });
}

onBeforeUnmount(() => {
  resetCopiedState();
});

</script>

<template>
  <DialogPrompt :open="showDeletePrompt" :mode="'delete'" :title-key="'deletePrompt.title'" :description-key="'deletePrompt.description'" @update:open="showDeletePrompt = false" @confirm="onDelete" />
    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full gap-4">
          <div class="flex items-center gap-4 min-w-0">
            <h1 v-if="changeNote.title" class="text-3xl md:text-4xl whitespace-nowrap text-ellipsis overflow-hidden leading-normal">{{
              changeNote.title }}</h1>
            <h1 v-else class="text-3xl text-text-primary/50 leading-normal">{{ t('placeholder.noTitle') }}</h1>
            <Tooltip v-if="isAdmin">
              <TooltipTrigger as-child>
                <Badge 
                  v-if="isAdmin" class="h-6"
                  :variant="changeNote.published ? 'success' : 'destructive'"
                >
                  {{ changeNote.published ? t('card.published') : t('card.private') }}
                </Badge>
              </TooltipTrigger>
              <TooltipContent>
                  {{ changeNote.published ? t('tooltip.publishedNote') : t('tooltip.privateNote') }}
              </TooltipContent>
            </Tooltip>
            <Tooltip v-if="isAdmin && changeNote.reference">
              <TooltipTrigger as-child>
                <Badge 
                  class="h-6 hover:cursor-pointer hover:underline"
                  variant="outline"
                  @click="() => openJiraTicket(changeNote.reference)"
                >
                  {{ changeNote.reference }}
                </Badge>
              </TooltipTrigger>
              <TooltipContent>
                  {{ t('tooltip.reference') }}
              </TooltipContent>
            </Tooltip>
          </div>
          <div class="flex gap-4 justify-center items-center">
            <Button :disabled="isTranslating" v-if="!(locale === 'en')" variant="glow" @click="onTranslate">{{hasTranslation ? t('button.undo') : t('button.translate') }}
              <Spinner v-if="isTranslating" class="h-4 dark:text-text-primary"/>
              <Sparkles v-else/> 
            </Button>
            <DropdownMenu v-if="isAdmin">
              <DropdownMenuTrigger
                class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                <EllipsisVertical class="text-text-primary" />
              </DropdownMenuTrigger>
              <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                <DropdownMenuItem @click="emit('update:modelValue', true)">
                  <div class="w-full flex gap-2">
                    <p class="text-text-primary ml-auto">{{ t('button.edit') }}</p>
                    <Pencil class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem @click="showDeletePrompt = true">
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-primary">{{ t('button.delete') }}</p>
                    <Trash2 class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem @click="onPublishToggle">
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-primary">{{ changeNote.published ? t('button.unpublish') : t('button.publish') }}</p>
                    <Eye class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
        <div class="flex justify-between">
        <p v-if="changeNote.description" v-html="md.render(translatedDescription ?? changeNote.description ?? '')"></p>
        <p v-else class="text-text-primary/50">{{ t('placeholder.noDescription') }}</p>
        <Button data-pdf-exclude class="size-fit" variant="outline" @click="handleCopy(hasTranslation ? translatedDescription ?? '' : changeNote.description, 'summary')">
          <component :is="copiedKey === 'summary' ? Check : Copy" />
        </Button>
        </div>
        <p v-if="hasTranslation" class="text-text-primary/50 text-right">{{ t('ai.translationDisclaimer') }}</p>
        <div class="flex justify-between">
        <div class="flex flex-wrap gap-4">
          <Tooltip v-if="changeNote.product">
            <TooltipTrigger as-child>
              <Badge  class="h-6">{{ changeNote.product.name }}</Badge>
            </TooltipTrigger>
            <TooltipContent>
                {{ t('title.product') }}
            </TooltipContent>
          </Tooltip>

          <Tooltip v-if="changeNote.scope">
            <TooltipTrigger as-child>
              <Badge  class="h-6">{{ changeNote.scope.name }}</Badge>
            </TooltipTrigger>
            <TooltipContent>
                {{ t('title.scope') }}
            </TooltipContent>
          </Tooltip>

          <Tooltip v-if="changeNote.feature">
            <TooltipTrigger as-child>
              <Badge  class="h-6">{{ changeNote.feature.name }}</Badge>
            </TooltipTrigger>
            <TooltipContent>
                {{ t('title.feature') }}
            </TooltipContent>
          </Tooltip>

          <Tooltip v-if="changeNote.customer">
            <TooltipTrigger as-child>
              <Badge  class="h-6">{{ changeNote.customer.name }}</Badge>
            </TooltipTrigger>
            <TooltipContent>
                {{ t('title.customer') }}
            </TooltipContent>
          </Tooltip>
        </div>
        <Tooltip v-if="changeNote.gitCommitHash && isAdmin">
          <TooltipTrigger as-child>
            <p :onclick="() => handleCopy(changeNote.gitCommitHash)" class="cursor-pointer text-text-primary/50 text-ellipsis overflow-hidden">{{ changeNote.gitCommitHash }}</p>
          </TooltipTrigger>
          <TooltipContent>
              Git commit hash
          </TooltipContent>
        </Tooltip>
        </div>
        <p v-if="changeNote.viewableByEveryone" class="text-text-primary/50">{{ t('changeNote.changeNoteViewableByEveryone') }}</p>
      </div>
      <Separator class="w-full h-2" />
      <div class="flex flex-col w-full text-xl gap-10">
        <div v-if="changeNote.developerNotes">
          <h3 class="text-2xl">{{ t('title.developerNotes') }}</h3>
          <div class="text-text-primary"  v-html="md.render(changeNote.developerNotes)"></div>
        </div>
        <div v-if="changeNote.upgradeNotes">
          <h3 class="text-2xl">{{ t('title.upgradeRequirements') }}</h3>
          <p  v-html="md.render(changeNote.upgradeNotes)"></p>
        </div>
      </div>
    </div>
</template>