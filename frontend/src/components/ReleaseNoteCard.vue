<script setup lang="ts">
import type { PrimitiveProps } from 'reka-ui';
import Badge from './ui/badge/Badge.vue';
import type { ReleaseNote } from '@/utils/types';
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import Tooltip from './ui/tooltip/Tooltip.vue';
import TooltipTrigger from './ui/tooltip/TooltipTrigger.vue';
import TooltipContent from './ui/tooltip/TooltipContent.vue';

const props = defineProps<PrimitiveProps & {
  releaseNote: ReleaseNote,
}>()

const releaseNote = props.releaseNote;
const { t } = useI18n();
</script>

<template>
  <RouterLink :to="`/release-notes/${releaseNote.id}`">
    <div
      class="flex flex-col p-4 gap-2 h-25 overflow-hidden max-w-full text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
      <div class="flex flex-row justify-between">
        <h3 v-if="releaseNote.tag" class="text-xl text-ellipsis overflow-hidden">{{ releaseNote.tag }}</h3>
        <h3 v-else class="text-xl text-text-primary/50">{{ t('placeholder.noTitle') }}</h3>

        <Tooltip v-if="isAdmin">
          <TooltipTrigger as-child>
            <Badge class="h-6" :variant="releaseNote.published ? 'success' : 'destructive'">
              {{ releaseNote.published ? t('card.published') : t('card.draft') }}
            </Badge>
          </TooltipTrigger>
          <TooltipContent>
            {{ releaseNote.published ? t('tooltip.publishedNote') : t('tooltip.draftNote') }}
          </TooltipContent>
        </Tooltip>
      </div>
      <div class="flex flex-row items-center gap-2 min-w-0">
        <p class="text-nowrap">{{ t('card.containsChangeNotes', { count: releaseNote.changeNotes.length }) }}</p>
        <Tooltip v-if="releaseNote.product">
          <TooltipTrigger as-child>
            <Badge variant="outline" class="truncate">{{ releaseNote.product.name }}</Badge>

          </TooltipTrigger>
          <TooltipContent>
            {{ t('title.product') }}
          </TooltipContent>
        </Tooltip>

      </div>
    </div>
  </RouterLink>
</template>
