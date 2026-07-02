<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { routeNames } from '@/utils/router';
import md from '@/utils/markdown-it';
import { openJiraTicket } from '@/utils/jira.ts';
import { useCopyToClipboard } from '@/composables/useCopyToClipboard';
import type { ChangeNote } from '@/utils/types';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { Check, Copy, Eye, EyeOff } from 'lucide-vue-next';

const props = defineProps<{
  changeNotes: ChangeNote[];
  generalChangesChecked: boolean;
  draftChangesChecked: boolean;
  customerFilter: number;
  translatedChangeNotes?: ChangeNote[] | null;
  hasTranslation?: boolean;
}>();

const deselectedIds = ref(new Set<number>());

const toggleSelected = (id: number) => {
  if (deselectedIds.value.has(id)) {
    deselectedIds.value.delete(id);
  } else {
    deselectedIds.value.add(id);
  }
};

const { t } = useI18n();
const { copiedKey, copy } = useCopyToClipboard();

const shouldShowChangeNote = (change: ChangeNote) => {
  if (!change.published && !props.draftChangesChecked) {
    return false;
  }
  if (change.customer === null) {
    return props.generalChangesChecked;
  }
  if (props.customerFilter === -1) {
    return true;
  }
  return change.customer.id === props.customerFilter;
};

const filteredChangeNotes = computed(() =>
  (props.translatedChangeNotes ?? props.changeNotes).filter(shouldShowChangeNote));

const selectedChangeNotes = computed(() =>
  filteredChangeNotes.value.filter(change => !deselectedIds.value.has(change.id)));

defineExpose({ selectedChangeNotes });
</script>

<template>
  <div class="flex flex-col gap-10 w-full">
    <p class="text-text-primary/50" v-if="changeNotes.length === 0">{{
      t('placeholder.noChangeNotesAdded')
      }}</p>
    <div v-for="change in filteredChangeNotes" :key="change.id" class="flex flex-col gap-2"
      :class="deselectedIds.has(change.id) ? 'opacity-50' : ''">
      <div class="flex items-center gap-4">
        <RouterLink
class="text-2xl dark:text-text-dark-static text-text-light-static hover:underline"
          :to="`${routeNames.changeNotes}/${change.id}`">{{ change.title || t('placeholder.noTitle') }}
        </RouterLink>

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
class="h-6 hover:cursor-pointer hover:underline" variant="outline"
              @click="() => openJiraTicket(change.reference)">
              {{ change.reference }}
            </Badge>
          </TooltipTrigger>
          <TooltipContent>
            {{ t('tooltip.reference') }}
          </TooltipContent>
        </Tooltip>

        <Tooltip>
          <TooltipTrigger as-child>
            <Button variant="outline" size="icon-sm" class="ml-auto" @click="toggleSelected(change.id)">
              <component :is="deselectedIds.has(change.id) ? EyeOff : Eye" />
            </Button>
          </TooltipTrigger>
          <TooltipContent>
            {{ deselectedIds.has(change.id) ? t('tooltip.includeInExport') : t('tooltip.excludeFromExport') }}
          </TooltipContent>
        </Tooltip>

      </div>
      <p v-if="change.viewableByEveryone" class="text-text-primary/50">{{
        t('changeNote.changeNoteViewableByEveryone') }}</p>
      <div>
        <div class="flex justify-between align-center">
          <div>
            <h3 class="text-xl">{{ t('title.description') }}</h3>
            <p class="ml-4" v-if="change.description" v-html="md.render(change.description)"></p>
          </div>
          <Button
variant="outline" size="icon-sm"
            @click="copy(hasTranslation ? translatedChangeNotes?.find(c => c.id === change.id)?.description ?? '' : change.description ?? '', `change-${change.id}`)">
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
  </div>
</template>
