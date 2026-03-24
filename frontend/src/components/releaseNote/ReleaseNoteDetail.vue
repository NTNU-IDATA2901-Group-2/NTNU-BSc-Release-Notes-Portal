<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue';
import { exportToPdf } from '@/utils/pdf';
import { useArchiveReleaseNote, usePublishReleaseNote } from '@/api/release-note-api';
import type { ChangeNote, ReleaseNote } from '@/utils/types';
import { useRouter } from 'vue-router';
import { routeNames } from '@/utils/router';
import { toast } from 'vue-sonner';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { Pencil, Trash2, Eye, EyeOff, FileDown, ArrowLeft, EllipsisVertical, Sparkles, Copy, Check } from "lucide-vue-next"
import md from '@/utils/markdown-it';
import DeletePrompt from '../DeletePrompt.vue';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Badge } from '../ui/badge';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '../ui/dropdown-menu';
import { Separator } from '../ui/separator';
import { Button } from '../ui/button';
import { useTranslate } from '@/api/ai';
import Spinner from '../ui/spinner/Spinner.vue';
import Checkbox from '../ui/checkbox/Checkbox.vue';

const props = defineProps<{
  releaseNote: ReleaseNote;
}>();

const isEditing = defineModel("isEditing", { type: Boolean, required: true });

const router = useRouter();
const { t, locale } = useI18n();

const releaseNote = props.releaseNote;

const deletePromptOpen = ref(false);

const { mutate: archiveReleaseNote } = useArchiveReleaseNote(releaseNote.id.toString(),
  {
    onSettled: () => {
      deletePromptOpen.value = false;
    },
    onSuccess: () => {
      router.push(routeNames.releaseNotes);
      toast.success(t('toast.releaseNoteDeletedSuccess'));
    },
    onError: () => {
      toast.error(t('toast.releaseNoteDeleteError'));
    }
  }
);

const startEditing = () => {
  isEditing.value = true;
}


const handlePublish = () => {
  if (releaseNote !== undefined) {
    publishReleaseNoteMutation.mutate({ id: releaseNote.id, publish: !releaseNote.published });
  }
}

const publishReleaseNoteMutation = usePublishReleaseNote({
  onSuccess: () => {
    toast.success(releaseNote.published ? t('toast.releaseNoteUnpublished') : t('toast.releaseNotePublished'));
  },
  onError: () => {
    toast.error(t('toast.releaseNotePublishError'));
  }
})

const releaseNoteRef = ref<HTMLDivElement>();

const handleExport = () => {
  if (!releaseNote) return;
  try {
    exportToPdf(releaseNote.tag, releaseNoteRef);
  } catch (error) {
    console.error('Error exporting to PDF:', error);
    toast.error(t('toast.exportPdfError'));
  }
}

const translateMutation = useTranslate({
  onSuccess: () => {
    toast.success(t('toast.translationSuccess'));
  },
  onError: () => {
    toast.error(t('toast.translationError'));
  },
});

const translatedChangeNotes = ref<ChangeNote[] | null>(null);
const translatedSummary = ref<string | null>(null);
const hasTranslation = ref(false);
const isTranslating = ref(false);

const onTranslate = async () => {
  if (translatedSummary.value || translatedChangeNotes.value) {
    translatedSummary.value = null;
    translatedChangeNotes.value = null;
    hasTranslation.value = false;
    return;
  }

  isTranslating.value = true;
  const summaryResult = await translateMutation.mutateAsync({
    text: releaseNote.summary || '',
    locale: locale.value,
  });

  translatedSummary.value = summaryResult;

  if (releaseNote.changeNotes) {
    releaseNote.changeNotes.forEach(async (changeNote, index) => {
      const result = await translateMutation.mutateAsync({
        text: changeNote.description,
        locale: locale.value,
      });

      if (result) {
        if (!translatedChangeNotes.value) translatedChangeNotes.value = [];
        translatedChangeNotes.value[index] = {
          ...changeNote,
          description: result,
        }
      }
    })
  }

  hasTranslation.value = true;
  isTranslating.value = false;
}

const generalReleasesChecked = ref(true);

const copiedKey = ref<string | null>(null);
let copyResetTimeout: ReturnType<typeof setTimeout> | null = null;

const resetCopiedState = () => {
  copiedKey.value = null;
  if (copyResetTimeout) {
    clearTimeout(copyResetTimeout);
    copyResetTimeout = null;
  }
};

const handleCopy = (text: string | null | undefined, key: string) => {
  if (!releaseNote) return;

  navigator.clipboard.writeText(text ?? '')
    .then(() => {
      copiedKey.value = key;
      if (copyResetTimeout) {
        clearTimeout(copyResetTimeout);
      }
      copyResetTimeout = setTimeout(() => {
        copiedKey.value = null;
        copyResetTimeout = null;
      }, 5000);
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
  <div class="flex flex-col w-full items-center px-4 mb-20">
    <DeletePrompt v-model:open="deletePromptOpen" :on-confirm="() => archiveReleaseNote()" />

    <div class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10 flex items-center gap-4">
      <Button variant="outline" class="" @click="$router.back()">
        <ArrowLeft />{{ t('button.previous') }}
      </Button>
      <Breadcrumb class="text-text-primary">
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/">{{ t('title.releaseNotes') }}</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            {{ releaseNote?.tag }}
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    </div>

    <div 
      ref="releaseNoteRef"
      class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center sm:gap-4">
            <h1 v-if="!releaseNote.tag" class="text-4xl text-text-primary/50">{{ t('placeholder.noTitle') }}</h1>
            <h1 v-else class="text-4xl max-w-60 whitespace-nowrap overflow-hidden">{{
              releaseNote.tag }}</h1>
            <Badge 
              data-pdf-exclude v-if="isAdmin" class="h-6"
              :variant="releaseNote.published ? 'success' : 'destructive'">{{
                releaseNote.published ? 'Published' : 'Private' }}</Badge>
          </div>
          <div data-pdf-exclude class="flex sm:gap-4">
            <Button 
              type="button" v-if="!(locale === 'en')" variant="glow" @click="onTranslate"
              :disabled="isTranslating" class="inline-flex items-center gap-2">
              {{ hasTranslation ? t('button.undo') : t('button.translate') }}
                <Spinner v-if="isTranslating" class="h-4 dark:text-text-primary"/>
                <Sparkles v-else/>
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger
                class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                <EllipsisVertical class="text-text-primary" />
              </DropdownMenuTrigger>
              <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                <DropdownMenuItem @click="startEditing" v-if="isAdmin">
                  <div class="w-full flex gap-2">
                    <p class="text-text-primary ml-auto">{{ t('button.edit') }}</p>
                    <Pencil class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem @click="deletePromptOpen = true" v-if="isAdmin">
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-primary">{{ t('button.delete') }}</p>
                    <Trash2 class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem @click="handlePublish" v-if="isAdmin">
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-primary">{{ !releaseNote.published ?
                      t('button.publish')
                      : t('button.unpublish') }}</p>
                    <component 
                      :is="!releaseNote.published ? Eye : EyeOff"
                      class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem @click="handleExport">
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-primary">{{ t('button.export') }}</p>
                    <FileDown class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <div class="flex justify-between">
        <p v-if="releaseNote.summary" v-html="md.render(translatedSummary ?? releaseNote.summary)">
        </p>
        <p class="text-text-primary/50" v-else>{{ t('placeholder.noSummary') }}</p>
        <Button data-pdf-exclude class="size-fit" variant="outline" @click="handleCopy(hasTranslation ? translatedSummary ?? '' : releaseNote.summary, 'summary')">
          <component :is="copiedKey === 'summary' ? Check : Copy" />
        </Button>
        </div>
        <p v-if="hasTranslation" class="text-text-primary/50 text-right">{{
          t('ai.translationDisclaimer') }}
        </p>
      </div>
      <Separator class="w-full h-2" />
      <div class="flex flex-col w-full gap-10">
        <div class="flex justify-between items-center">
          <h2 class="text-3xl">{{ t('title.changeNotes') }}</h2>
          <div class="flex gap-2">
            <p data-pdf-exclude>{{ t('button.showGeneralChanges') }}</p>
            <Checkbox data-pdf-exclude v-model="generalReleasesChecked"/>
          </div>
        </div>
        <div class="flex flex-col gap-16">
          <div class="flex flex-col gap-10">
            <p class="text-text-primary/50" v-if="releaseNote.changeNotes.length === 0">{{ t('placeholder.noChangeNotesAdded') }}</p>
            <template 
              v-for="change in translatedChangeNotes ?? releaseNote.changeNotes" :key="change.id"
              >
              <div v-if="generalReleasesChecked || change.customer !== null">
                <div class="flex items-center gap-4">
                  <RouterLink class="text-2xl text-text-dark-static hover:underline" :to="`${routeNames.changeNotes}/${change.id}`">{{ change.reference }}</RouterLink>

                  <Badge v-if="change.customer" :variant="'outline'">{{ change.customer.name }}</Badge>
                </div>
                <div>
                  <h3 class="text-xl" data-pdf-exclude>{{ t('title.description') }}</h3>
                  <div class="flex justify-between">
                    <p class="ml-4" v-if="change.description" v-html="md.render(change.description)"></p>
                    <Button data-pdf-exclude variant="outline" size="icon-sm" @click="handleCopy(hasTranslation ? translatedChangeNotes?.find(c => c.id === change.id)?.description ?? '' : change.description ?? '', `change-${change.id}`)">
                      <component :is="copiedKey === `change-${change.id}` ? Check : Copy" />
                    </Button>
                  </div>
                  <p v-if="hasTranslation" class="text-text-primary/50 text-right" data-pdf-exclude>{{
                    t('ai.translationDisclaimer') }}</p>
                </div>
                <div v-if="change.developerNotes" data-pdf-exclude>
                  <h3 class="text-xl">{{ t('title.developerNotes') }}</h3>
                  <p class="ml-4" v-html="md.render(change.developerNotes)"></p>
                </div>
                <div v-if="change.upgradeNotes" data-pdf-exclude>
                  <h3 class="text-xl">{{ t('title.upgradeRequirements') }}</h3>
                  <p class="ml-4" v-html="md.render(change.upgradeNotes)"></p>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>