<script setup lang="ts">
import type { PrimitiveProps } from 'reka-ui';
import Badge from './ui/badge/Badge.vue';
import type { ReleaseNote } from '@/utils/types';
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';

const props = defineProps<PrimitiveProps & {
  releaseNote: ReleaseNote,
}>()

const releaseNote = props.releaseNote;
const { t } = useI18n();
</script>

<template>
  <RouterLink :to="`/release-notes/${releaseNote.id}`" >
    <div
      class="flex flex-col p-4 gap-2 h-30 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
      <div class="flex flex-row justify-between">
        <h3 class="text-xl">{{ releaseNote.tag }}</h3>
        <Badge v-if="isAdmin" :variant="releaseNote.published ? 'success' : 'destructive'">{{ releaseNote.published
          ? t('card.published') : t('card.private') }}</Badge>
      </div>
      <p>{{ t('card.containsChangeNotes', { count: releaseNote.changeNotes.length }) }}</p>
    </div>
  </RouterLink>
</template>