<script setup lang="ts">
import { Checkbox } from '@/components/ui/checkbox';
import { computed, inject, onMounted, type Ref } from 'vue';

const props = defineProps<{
    label: string,
    queryKey: string,
    value: string,
    initialState?: boolean,
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

const applyInitialState = () => {
  if (props.initialState && searchParams.value[props.queryKey] === undefined) {
    searchParams.value[props.queryKey] = props.value;
  }
};

onMounted(() => {
  applyInitialState();
});
</script>

<template>
  <div class="flex items-center gap-3">
    <Checkbox :model-value="isInParams" @update:model-value="update" class="cursor-pointer"/>
    <p class="text-sm">{{ props.label }}</p>
  </div>
</template>