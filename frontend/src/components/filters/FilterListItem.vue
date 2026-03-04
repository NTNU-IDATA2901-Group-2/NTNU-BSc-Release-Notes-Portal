<script setup lang="ts">
import { Checkbox } from '@/components/ui/checkbox';
import { computed, inject, type Ref } from 'vue';

const props = defineProps<{
    label: string,
    queryKey: string,
    value: string,
}>()

const searchParams = inject('searchParams') as Ref<{ [key: string]: string }>;
const isInParams = computed(() => {
  const params = searchParams.value[props.queryKey]?.split(',') || [];
  return params.includes(props.value);
});

const update = (value: string | boolean) => {
  const params = searchParams.value[props.queryKey]?.split(',') || [];
  if (typeof value === 'boolean') {
    if (value) {
      if (!params.includes(props.value)) {
        params.push(props.value);
        searchParams.value[props.queryKey] = params.join(',');
      }
    } else {
      if (params.includes(props.value)) {
        const newParams = params.filter(param => param !== props.value);
        if (newParams.length > 0) {
          searchParams.value[props.queryKey] = newParams.join(',');
        } else {
          delete searchParams.value[props.queryKey];
        }
      }
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