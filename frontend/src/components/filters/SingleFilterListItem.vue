<script setup lang="ts">
import { Checkbox } from '@/components/ui/checkbox';
import { computed, inject, type Ref } from 'vue';

const props = defineProps<{
    label: string,
    queryKey: string,
    value: string,
}>()

const searchParams = inject('searchParams') as Ref<{ [key: string]: string }>;
const isInParams = computed(() => searchParams.value[props.queryKey] === props.value);

const update = (value: string | boolean) => {
  if (typeof value === 'boolean') {
    if (value) {
      searchParams.value[props.queryKey] = props.value;
    } else {
      delete searchParams.value[props.queryKey];
    }
  }
}

</script>

<template>
  <div class="flex items-center gap-3">
    <Checkbox :model-value="isInParams" @update:model-value="update" class="cursor-pointer"/>
    <p class="text-sm">{{ props.label }}</p>
  </div>
</template>