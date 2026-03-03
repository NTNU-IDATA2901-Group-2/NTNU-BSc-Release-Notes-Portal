<script lang="tsx" setup>
import FilterListItem from './FilterListItem.vue';

const props = defineProps<{
  selectable: string[]
}>();

const selectedItems = defineModel<string[]>({
  default: () => []
});

const toggle = (label : string, value: string | boolean) => {
  if (typeof value === 'boolean') {
    if (value) {
      selectedItems.value = [...selectedItems.value, label];
    } else {
      selectedItems.value = selectedItems.value.filter(item => item !== label);
    }
  }
};

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-lg">Product</h4>
      <FilterListItem v-for="value in props.selectable" :key="value" :label="value" :selected="selectedItems.includes(value)" @update:selected="(bolval) => toggle(value, bolval)"/>
    </div>
</template>