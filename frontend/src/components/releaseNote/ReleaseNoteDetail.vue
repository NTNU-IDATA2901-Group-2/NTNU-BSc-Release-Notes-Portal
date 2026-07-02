<script setup lang="ts">
import { computed, ref } from 'vue';
import { exportToPdf, type PdfVariant } from '@/utils/pdf';
import { useArchiveReleaseNote, usePublishReleaseNote } from '@/api/release-note-api';
import type { ChangeNote, ReleaseNote, ChangeImpact } from '@/utils/types';
import { useRouter } from 'vue-router';
import { routeNames } from '@/utils/router';
import { toast } from 'vue-sonner';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { Pencil, Trash2, Eye, EyeOff, FileDown, ArrowLeft, EllipsisVertical, Sparkles, Copy, Check, GitBranch } from "lucide-vue-next"
import md from '@/utils/markdown-it';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Badge } from '../ui/badge';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSub, DropdownMenuSubContent, DropdownMenuSubTrigger, DropdownMenuTrigger } from '../ui/dropdown-menu';
import { Separator } from '../ui/separator';
import { Button } from '../ui/button';
import { useTranslate } from '@/api/ai-api';
import Spinner from '../ui/spinner/Spinner.vue';
import DialogPrompt from '../DialogPrompt.vue';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import ScrollArea from '../ui/scroll-area/ScrollArea.vue';
import { getLabelFromChangeNote, uniqueCustomers } from '@/utils/change-note.ts';
import { useSyncReleaseNoteToGit } from '@/api/git-repository-api.ts';
import { useGetJiraServiceRequestKeys } from '@/api/jira-api.ts';
import { getLocaleDateString } from '@/utils/format-date.ts';
import ChangeImpactTable from '../ChangeImpactTable.vue';
import ChangeNoteList from '../changeNote/ChangeNoteList.vue';
import ChangeNoteListFilters from '../changeNote/ChangeNoteListFilters.vue';
import { useCopyToClipboard } from '@/composables/useCopyToClipboard';

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

const commitPromptOpen = ref(false);
const commitReleaseNoteToGit = useSyncReleaseNoteToGit({
  onSuccess: () => {
    toast.success(t('toast.commitToGitSuccess'));
  },
  onError: () => {
    toast.error(t('toast.commitToGitError'));
  },
  onSettled: () => {
    commitPromptOpen.value = false;
  }
})

let hasToastedSuccess = false
const translateMutation = useTranslate({
  onSuccess: () => {
    if (!hasToastedSuccess) {
      toast.success(t('toast.releaseNoteTranslationSuccess'));
      hasToastedSuccess = true;
    }
  },
  onError: () => {
    toast.error(t('toast.translationError'));
  },
});

const translatedChangeImpacts = ref<ChangeImpact[] | null>(null);
const translatedKnownLimitations = ref<string[] | null>(null);
const translatedChangeNotes = ref<ChangeNote[] | null>(null);
const translatedSummary = ref<string | null>(null);
const hasTranslation = ref(false);
const isTranslating = ref(false);

const onTranslate = async () => {
  if (translatedSummary.value || translatedKnownLimitations.value || translatedChangeImpacts.value || translatedChangeNotes.value) {
    translatedSummary.value = null;
    translatedChangeImpacts.value = null;
    translatedKnownLimitations.value = null;
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

  if (releaseNote.changeImpacts) {
    translatedChangeImpacts.value = await Promise.all(
      releaseNote.changeImpacts.map(async (changeImpact) => {
        const whatIsChanged = !changeImpact.whatIsChanged ? "" : await translateMutation.mutateAsync({
          text: changeImpact.whatIsChanged,
          locale: locale.value,
        });
        const whatShouldBeTested = !changeImpact.whatShouldBeTested ? "" : await translateMutation.mutateAsync({
          text: changeImpact.whatShouldBeTested,
          locale: locale.value,
        });
        return {
          ...changeImpact,
          whatIsChanged: whatIsChanged || changeImpact.whatIsChanged,
          whatShouldBeTested: whatShouldBeTested || changeImpact.whatShouldBeTested,
        }
      })
    )
  }

  if (releaseNote.knownLimitations) {
    translatedKnownLimitations.value = await Promise.all(
      releaseNote.knownLimitations.map(async (limitation) => {
        return !limitation ? "" : await translateMutation.mutateAsync({
          text: limitation,
          locale: locale.value
        })
      }
      )
    )
  }

  if (releaseNote.changeNotes) {
    releaseNote.changeNotes.forEach(async (changeNote, index) => {
      const result = !changeNote.description ? "" : await translateMutation.mutateAsync({
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
  hasToastedSuccess = false;
}

const { mutateAsync: fetchServiceRequestKeys, isPending: isPendingServiceRequestKeys } = useGetJiraServiceRequestKeys();

const handleExport = async (variants: PdfVariant[] = ['customer', 'technical']) => {
  if (!releaseNote) return;
  try {
    const changeNotes = changeNoteList.value?.selectedChangeNotes ?? [];
    const references = [...new Set(
      changeNotes.map(change => change.reference).filter(reference => reference && reference.length > 0),
    )];
    const serviceRequestKeys = await fetchServiceRequestKeys(references);
    exportToPdf(releaseNote, changeNotes, serviceRequestKeys, variants);
  } catch (error) {
    console.error('Error exporting to PDF:', error);
    toast.error(t('toast.exportPdfError'));
  }
}

const { copiedKey, copy } = useCopyToClipboard();

const changeNoteList = ref<InstanceType<typeof ChangeNoteList> | null>(null);

const generalChangesChecked = ref(true);
const draftChangesChecked = ref(true);
const customerFilter = ref<number>(-1);

const changeNoteCustomers = computed(() => uniqueCustomers(releaseNote.changeNotes));
</script>

<template>
  <div class="flex flex-col w-full items-center px-4 mb-20">
    <DialogPrompt
v-model:open="deletePromptOpen" :mode="'delete'" :title-key="'deletePrompt.title'"
      :description-key="'deletePrompt.description'" :on-confirm="() => archiveReleaseNote()" />
    <DialogPrompt
v-model:open="commitPromptOpen" :mode="'confirm'" :title-key="'commitPrompt.title'"
      :description-key="'commitPrompt.description'"
      :on-confirm="() => commitReleaseNoteToGit.mutate({ id: releaseNote.id, additionalGitRepositoryIds: [] })">
      <ScrollArea class="max-h-60 border">
        <p
v-for="change in releaseNote.changeNotes" :key="change.id"
          :class="(!change.title && !change.reference) ? 'text-text-primary/50' : ''">
          {{ getLabelFromChangeNote(change) || t('placeholder.noTitle') }}
        </p>
      </ScrollArea>
    </DialogPrompt>
    <div
      class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10 flex items-center gap-4 max-w-[calc(100%-2rem)] lg:max-w-[calc(100%-5rem)]">
      <Button variant="outline" class="shrink-0" @click="$router.back()">
        <ArrowLeft />{{ t('button.previous') }}
      </Button>
      <Breadcrumb class="text-text-primary min-w-0">
        <BreadcrumbList class="min-w-0">
          <BreadcrumbItem class="shrink-0">
            <BreadcrumbLink href="/">{{ t('title.releaseNotes') }}</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator class="shrink-0" />
          <BreadcrumbItem class="min-w-0">
            <span class="truncate">{{ releaseNote?.tag ?? t('placeholder.noTitle') }}</span>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    </div>

    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-col sm:flex-row items-start justify-between max-w-full gap-4">
          <h1 v-if="!releaseNote.tag" class="text-4xl text-text-primary/50 leading-normal">{{ t('placeholder.noTitle')
          }}</h1>
          <h1 v-else class="text-3xl md:text-4xl truncate max-w-full leading-normal">{{
            releaseNote.tag }}
          </h1>
          <div class="flex sm:gap-4 w-full sm:w-auto sm:grow items-center self-center">
            <div class="flex flex-row gap-2 items-center mr-auto">
              <Tooltip v-if="isAdmin">
                <TooltipTrigger as-child>
                  <Badge class="h-6 w-fit" :variant="releaseNote.published ? 'success' : 'destructive'">
                    {{ releaseNote.published ? t('card.published') : t('card.draft') }}
                  </Badge>
                </TooltipTrigger>
                <TooltipContent>
                  {{ releaseNote.published ? t('tooltip.publishedNote') : t('tooltip.draftNote') }}
                </TooltipContent>
              </Tooltip>
              <Tooltip v-if="releaseNote.product">
                <TooltipTrigger as-child>
                  <Badge variant="outline" class="w-fit">{{ releaseNote.product.name }}</Badge>
                </TooltipTrigger>
                <TooltipContent>
                  {{ t('title.product') }}
                </TooltipContent>
              </Tooltip>

            </div>
            <Button
type="button" v-if="!(locale === 'en-GB')" variant="glow" @click="onTranslate"
              :disabled="isTranslating" class="inline-flex items-center gap-2">
              {{ hasTranslation ? t('button.undo') : t('button.translate') }}
              <Spinner v-if="isTranslating" class="h-4 dark:text-text-primary" />
              <Sparkles v-else />
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
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
                    <component :is="!releaseNote.published ? Eye : EyeOff" class="text-text-primary" />
                  </div>
                </DropdownMenuItem>
                <Tooltip>
                  <TooltipTrigger as-child>
                    <DropdownMenuItem
                      @click="(!releaseNote.syncedToGit && releaseNote.changeNotes.length > 0) ? commitPromptOpen = true : null"
                      v-if="isAdmin"
                      :class="releaseNote.syncedToGit || releaseNote.changeNotes.length === 0 ? 'cursor-default opacity-50 focus:bg-transparent ' : ''">
                      <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-primary">{{ t('button.commitToGit') }}</p>
                        <GitBranch class="text-text-primary" />
                      </div>
                    </DropdownMenuItem>
                  </TooltipTrigger>
                  <TooltipContent v-if="releaseNote.syncedToGit">{{ t('tooltip.alreadyCommited') }}</TooltipContent>
                  <TooltipContent v-else-if="releaseNote.changeNotes.length === 0">{{ t('tooltip.noChangeNotesToCommit')
                  }}</TooltipContent>
                </Tooltip>
                <DropdownMenuSub>
                  <DropdownMenuSubTrigger @click="handleExport()" class="focus:bg-text-primary/10 cursor-pointer">
                    <div class="flex gap-2 items-center">
                      <p class="text-text-primary">{{ releaseNote.published ? t('button.exportRelease') :
                        t('button.exportPreview') }}</p>
                      <Spinner v-if="isPendingServiceRequestKeys" class="size-4 my-0" />
                      <FileDown v-else class="text-text-primary" />
                    </div>
                  </DropdownMenuSubTrigger>
                  <DropdownMenuSubContent>
                    <DropdownMenuItem @click="handleExport()">
                      <p class="text-text-primary">{{ t('button.exportBoth') }}</p>
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="handleExport(['customer'])">
                      <p class="text-text-primary">{{ t('button.exportCustomer') }}</p>
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="handleExport(['technical'])">
                      <p class="text-text-primary">{{ t('button.exportTechnical') }}</p>
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuSub>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <div class="flex justify-between">
          <p v-if="releaseNote.summary" v-html="md.render(translatedSummary ?? releaseNote.summary)">
          </p>
          <p class="text-text-primary/50" v-else>{{ t('placeholder.noSummary') }}</p>
          <Button
class="size-fit" variant="outline"
            @click="copy(hasTranslation ? translatedSummary ?? '' : releaseNote.summary, 'summary')">
            <component :is="copiedKey === 'summary' ? Check : Copy" />
          </Button>
        </div>
        <p v-if="hasTranslation" class="text-text-primary/50 text-right">{{
          t('ai.translationDisclaimer') }}
        </p>
      </div>
      <div class="flex flex-col gap-16 w-full">
        <Separator class="w-full h-2" />
        <div class="flex flex-col gap-4 w-full">
          <h2 class="text-3xl truncate max-w-full leading-normal">{{ t('title.releaseTimeline') }}</h2>
          <div class="flex flex-col gap-4">
            <span class="flex flex-row gap-2">
              <p class="text-md">{{ `${t('title.previewAvailableFrom')}: ` }}</p>
              <p v-if="releaseNote.releaseTimeline?.previewAvailableFrom">
                {{ getLocaleDateString(releaseNote.releaseTimeline.previewAvailableFrom) }}
              </p>
              <p v-else class="text-text-primary/50">{{ t('placeholder.toBeDetermined') }}</p>
            </span>
            <span class="flex flex-row gap-2">
              <p class="text-md">{{ `${t('title.recommendedTestPhase')}: ` }}</p>
              <p v-if="!releaseNote.releaseTimeline" class="text-text-primary/50">{{ t('placeholder.toBeDetermined') }}
              </p>
              <template v-else>
                <p v-if="releaseNote.releaseTimeline.recommendedTestPhaseFrom">
                  {{ getLocaleDateString(releaseNote.releaseTimeline.recommendedTestPhaseFrom) }}
                </p>
                <p v-else class="text-text-primary/50">{{ t('placeholder.toBeDetermined') }}</p>
                <p>-</p>
                <p v-if="releaseNote.releaseTimeline.recommendedTestPhaseTo">
                  {{ getLocaleDateString(releaseNote.releaseTimeline.recommendedTestPhaseTo) }}
                </p>
                <p v-else class="text-text-primary/50">{{ t('placeholder.toBeDetermined') }}</p>
              </template>
            </span>
            <span class="flex flex-row gap-2">
              <p class="text-md">{{ `${t('title.plannedProductionDeployment')}: ` }}</p>
              <p v-if="releaseNote.releaseTimeline?.plannedProductionDeployment">
                {{ getLocaleDateString(releaseNote.releaseTimeline.plannedProductionDeployment) }}
              </p>
              <p v-else class="text-text-primary/50">{{ t('placeholder.toBeDetermined') }}</p>
            </span>
          </div>
        </div>
        <template v-if="isAdmin">
          <Separator class="w-full h-2" />
          <div class="flex flex-col gap-4 w-full">
            <h2 class="text-3xl truncate max-w-full leading-normal">{{ t('title.changeImpacts') }}</h2>
            <ChangeImpactTable :model-value="translatedChangeImpacts ?? releaseNote.changeImpacts" />
          </div>
        </template>
        <template v-if="isAdmin">
          <Separator class="w-full h-2" />
          <div class="flex flex-col gap-4 w-full">
            <h2 class="text-3xl truncate max-w-full leading-normal">{{ t('title.knownLimitations') }}</h2>
            <ul v-if="releaseNote.knownLimitations?.length" class="list-disc pl-6 flex flex-col gap-2">
              <li
v-for="(limitation, index) in translatedKnownLimitations ?? releaseNote.knownLimitations"
                :key="index">
                {{ limitation }}
              </li>
            </ul>
            <p v-else class="text-text-primary/50">{{ t('placeholder.noKnownLimitations') }}</p>
          </div>
        </template>
      </div>
      <Separator class="w-full h-2" />
      <div class="flex flex-col w-full gap-10">
        <div class="flex gap-4 flex-col md:flex-row justify-between items-start">
          <h2 class="text-3xl">{{ t('title.changeNotes') }}</h2>
          <ChangeNoteListFilters
v-model:general-changes-checked="generalChangesChecked"
            v-model:draft-changes-checked="draftChangesChecked" v-model:customer-filter="customerFilter"
            :customers="changeNoteCustomers" />
        </div>
        <ChangeNoteList
ref="changeNoteList" :change-notes="releaseNote.changeNotes"
          :general-changes-checked="generalChangesChecked" :draft-changes-checked="draftChangesChecked"
          :customer-filter="customerFilter" :translated-change-notes="translatedChangeNotes"
          :has-translation="hasTranslation" />
      </div>
    </div>
  </div>
</template>
