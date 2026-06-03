<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { exportToPdf } from '@/utils/pdf';
import { useArchiveReleaseNote, usePublishReleaseNote } from '@/api/release-note-api';
import type { ChangeNote, Customer, ReleaseNote } from '@/utils/types';
import { useRouter } from 'vue-router';
import { routeNames } from '@/utils/router';
import { toast } from 'vue-sonner';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { Pencil, Trash2, Eye, EyeOff, FileDown, ArrowLeft, EllipsisVertical, Sparkles, Copy, Check } from "lucide-vue-next"
import md from '@/utils/markdown-it';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Badge } from '../ui/badge';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '../ui/dropdown-menu';
import { Separator } from '../ui/separator';
import { Button } from '../ui/button';
import { useTranslate } from '@/api/ai-api';
import Spinner from '../ui/spinner/Spinner.vue';
import Checkbox from '../ui/checkbox/Checkbox.vue';
import DialogPrompt from '../DialogPrompt.vue';
import Select from '../ui/select/Select.vue';
import SelectTrigger from '../ui/select/SelectTrigger.vue';
import SelectValue from '../ui/select/SelectValue.vue';
import SelectContent from '../ui/select/SelectContent.vue';
import SelectGroup from '../ui/select/SelectGroup.vue';
import SelectItem from '../ui/select/SelectItem.vue';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { openJiraTicket } from '@/utils/jira.ts';

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

const handleExport = () => {
  if (!releaseNote) return;
  try {
    const filteredChangeNotes = (translatedChangeNotes.value ?? releaseNote.changeNotes).filter(shouldShowChangeNote);
    exportToPdf(releaseNote.tag, releaseNote.summary, filteredChangeNotes);
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

const uniqueCustomers = computed(() => {
  const customerArray = new Array<Customer>();
  releaseNote.changeNotes.forEach(change => {
    if (change.customer) {
      if (!customerArray.some(c => c.id === change.customer?.id)) {
        customerArray.push(change.customer);
      }
    }
  });
  return Array.from(customerArray);
})

const shouldShowChangeNote = (change: ChangeNote) => {
  if (change.customer === null) {
    return generalReleasesChecked.value;
  }

  if (customerFilter.value === -1) {
    return true;
  }

  if (change.customer.id === customerFilter.value) {
    return true;
  }
  return false;
}

const customerFilter = ref<number>(-1);
</script>

<template>
  <div class="flex flex-col w-full items-center px-4 mb-20">
    <DialogPrompt v-model:open="deletePromptOpen" :mode="'delete'" :title-key="'deletePrompt.title'" :description-key="'deletePrompt.description'" :on-confirm="() => archiveReleaseNote()" />

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
      class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between max-w-full gap-4">
          <div class="flex items-center gap-4 min-w-0">
            <h1 v-if="!releaseNote.tag" class="text-4xl text-text-primary/50">{{ t('placeholder.noTitle') }}</h1>
            <h1 v-else class="text-3xl md:text-4xl whitespace-nowrap text-ellipsis overflow-hidden">{{
              releaseNote.tag }}
            </h1>
            <Tooltip v-if="isAdmin">
              <TooltipTrigger as-child>
                <Badge 
                  class="h-6 w-fit"
                  :variant="releaseNote.published ? 'success' : 'destructive'"
                >
                  {{ releaseNote.published ? t('card.published') : t('card.private') }}
                </Badge>
              </TooltipTrigger>
              <TooltipContent>
                  {{ releaseNote.published ? t('tooltip.publishedNote') : t('tooltip.privateNote') }}
              </TooltipContent>
            </Tooltip>
            
          </div>
          <div class="flex sm:gap-4 w-fit">
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
        <Button class="size-fit" variant="outline" @click="handleCopy(hasTranslation ? translatedSummary ?? '' : releaseNote.summary, 'summary')">
          <component :is="copiedKey === 'summary' ? Check : Copy" />
        </Button>
        </div>
        <p v-if="hasTranslation" class="text-text-primary/50 text-right">{{
          t('ai.translationDisclaimer') }}
        </p>
      </div>
      <Separator class="w-full h-2" />
      <div class="flex flex-col w-full gap-10">
        <div class="flex gap-4 flex-col md:flex-row justify-between items-start">
          <h2 class="text-3xl">{{ t('title.changeNotes') }}</h2>
          <div class="flex flex-row items-center gap-4">
            <div class="flex gap-2">
              <p>{{ t('button.showGeneralChanges') }}</p>
              <Checkbox v-model="generalReleasesChecked" class="cursor-pointer"/>
            </div>
            <div>
              <Select v-model="customerFilter">
                <SelectTrigger class="w-42">
                  <SelectValue placeholder="Filter by customer"/>
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem :value=-1 class="text-text-primary/50">
                      {{ t('button.allCustomers') }}
                    </SelectItem>
                  </SelectGroup>
                  <SelectGroup>
                    <SelectItem v-for="customer in uniqueCustomers" :key="customer.id" :value="customer.id">
                      {{ customer.name }}
                    </SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

          </div>
        </div>
        <div class="flex flex-col gap-16">
          <div class="flex flex-col gap-10">
            <p class="text-text-primary/50" v-if="releaseNote.changeNotes.length === 0">{{ t('placeholder.noChangeNotesAdded') }}</p>
            <template
              v-for="change in translatedChangeNotes ?? releaseNote.changeNotes" :key="change.id"
              >
              <div v-if="shouldShowChangeNote(change)" class="flex flex-col gap-2">
                <div class="flex items-center gap-4">
                  <RouterLink class="text-2xl dark:text-text-dark-static text-text-light-static hover:underline" :to="`${routeNames.changeNotes}/${change.id}`">{{ change.title || t('placeholder.noTitle') }}</RouterLink>

                  <Tooltip v-if="change.customer">
                    <TooltipTrigger as-child>
                      <Badge v-if="change.customer" :variant="'outline'">{{ change.customer.name }}</Badge>
                    </TooltipTrigger>
                    <TooltipContent>
                        {{ t('title.customer') }}
                    </TooltipContent>
                  </Tooltip>

                  <Tooltip v-if="isAdmin && change.reference">
                    <TooltipTrigger as-child>
                      <Badge 
                        class="h-6 hover:cursor-pointer hover:underline"
                        variant="outline"
                        @click="() => openJiraTicket(change.reference)"
                      >
                        {{ change.reference }}
                      </Badge>
                    </TooltipTrigger>
                    <TooltipContent>
                        {{ t('tooltip.reference') }}
                    </TooltipContent>
                  </Tooltip>
                  
                </div>
                <p v-if="change.viewableByEveryone" class="text-text-primary/50">{{ t('changeNote.changeNoteViewableByEveryone') }}</p>
                <div>
                  <div class="flex justify-between align-center">
                    <div>
                      <h3 class="text-xl">{{ t('title.description') }}</h3>
                      <p class="ml-4" v-if="change.description" v-html="md.render(change.description)"></p>
                    </div>
                    <Button variant="outline" size="icon-sm" @click="handleCopy(hasTranslation ? translatedChangeNotes?.find(c => c.id === change.id)?.description ?? '' : change.description ?? '', `change-${change.id}`)">
                      <component :is="copiedKey === `change-${change.id}` ? Check : Copy" />
                    </Button>
                  </div>
                  <p v-if="hasTranslation" class="text-text-primary/50 text-right">{{
                    t('ai.translationDisclaimer') }}</p>
                </div>
                <div v-if="change.developerNotes">
                  <h3 class="text-xl">{{ t('title.developerNotes') }}</h3>
                  <p class="ml-4" v-html="md.render(change.developerNotes)"></p>
                </div>
                <div v-if="change.upgradeNotes">
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