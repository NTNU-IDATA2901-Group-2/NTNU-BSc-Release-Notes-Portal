<script lang="tsx" setup>
import FilterListItem from './FilterListItem.vue';
import { useFeatures } from '@/api/features-api';

const { data: products, isLoading, isError } = useFeatures();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-lg">Feature</h4>
      <p v-if="isLoading">Loading features...</p>
      <p v-else-if="isError">Failed to load features.</p>
      <FilterListItem v-else v-for="product in products" :key="product.id" :query-key="'featureId'" :value="product.id.toString()" :label="product.name"/>
    </div>
</template>