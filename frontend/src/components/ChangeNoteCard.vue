<script setup lang="ts">
import type { PrimitiveProps } from 'reka-ui';
import Badge from './ui/badge/Badge.vue';
import type { ChangeNote } from '@/utils/types';
import { Checkbox } from './ui/checkbox';

const props = defineProps<PrimitiveProps & {
  changeNote: ChangeNote,
  selected: boolean
}>()

const changeNote = props.changeNote;

// Hack for making checkbox work inside a RouterLink. Prevents the click event from propagating to the RouterLink, allowing the checkbox to be toggled without navigating to the change note details page. Also emits an update:selected event to the parent component to update the selected state of the change note card.
const emit = defineEmits<{
  'update:selected': [value: boolean]
}>()

const handleCheckboxClick = (event: Event) => {
  event.preventDefault();
  event.stopPropagation();
  emit('update:selected', !props.selected);
}

</script>

<template>
  <RouterLink :to="`/change-notes/${changeNote.id}`" class="block">
    
    <div
      class="flex flex-col p-4 gap-2 h-30 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
      <div class="flex justify-between">
        <div class="flex gap-4 items-center">

          <div @click="handleCheckboxClick" @keydown.enter="handleCheckboxClick" class="flex items-center">
            <Checkbox :checked="$props.selected" :value="$props.changeNote.id" :disabled="false" />
          </div>

          <h3 class="text-xl">{{ changeNote.reference }}</h3>

          <Badge v-if="changeNote.customer" :variant="'outline'">{{ changeNote.customer.name }}</Badge>

        </div>
        <div>
          <Badge :variant="changeNote.published ? 'success' : 'destructive'">{{ changeNote.published
            ? 'Published' : 'Private' }}</Badge>
        </div>
      </div>

      <p>{{ changeNote.description }}</p>

      <div class="flex flex-row gap-2">
        <Badge v-if="changeNote.scope" :variant="'default'">{{ changeNote.scope.name }}</Badge>
        <Badge v-if="changeNote.feature" :variant="'default'">{{ changeNote.feature.name }}</Badge>
        <Badge v-if="changeNote.product" :variant="'default'">{{ changeNote.product.name }}</Badge>
      </div>

    </div>
  </RouterLink>
</template>