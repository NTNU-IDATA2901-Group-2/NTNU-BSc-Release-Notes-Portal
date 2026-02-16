<script setup lang="ts">
import type { PrimitiveProps } from 'reka-ui';
import Badge from './ui/badge/Badge.vue';
import type { ReleaseNote } from '@/types';
import { RouterLink } from 'vue-router';

const props = defineProps<PrimitiveProps & {
  releaseNote: ReleaseNote,
}>()

const releaseNote = props.releaseNote;
</script>

<template>
  <RouterLink :to="`/release-notes/${releaseNote.id}`" >
    <div
      class="flex flex-col p-4 gap-2 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
      <div class="flex flex-row justify-between">
        <h3 class="text-xl">{{ releaseNote.version }}</h3>
        <Badge :variant="releaseNote.published ? 'success' : 'destructive'">{{ releaseNote.published
          ? 'Published' : 'Private' }}</Badge>
      </div>
      <p>{{ releaseNote.description }}</p>
    </div>
  </RouterLink>
</template>