<script setup lang="ts">
import { Checkbox } from '@/components/ui/checkbox';

const props = defineProps<{
    label: string,
    selected: boolean
}>()

const emit = defineEmits<(e : 'update:selected', value: string | boolean) => void>();

</script>

<template>
  <div class="flex items-center gap-3">
    <Checkbox :model-value="props.selected" @update:model-value="(value) => emit('update:selected', value)"/>
    <p class="text-sm">{{ props.label }}</p>
  </div>
</template>