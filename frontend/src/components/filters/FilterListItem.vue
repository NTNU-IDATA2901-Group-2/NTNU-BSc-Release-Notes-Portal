<script setup lang="ts">
import { Checkbox } from '@/components/ui/checkbox';

const checked = defineModel<boolean>({ default: false });

defineProps<{
    label: string,
}>()
</script>

<template>
  <div class="flex items-center gap-3">
    <Checkbox :model-value="checked" @update:model-value="(value) => checked = value === true" class="cursor-pointer"/>
    <p class="text-sm">{{ label }}</p>
  </div>
</template>
