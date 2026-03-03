<script lang="tsx" setup>
import { useScopes } from '@/api/scopes-api';
import FilterListItem from './FilterListItem.vue';

const { data: products, isLoading, isError } = useScopes();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-lg">Scope</h4>
      <p v-if="isLoading">Loading scopes...</p>
      <p v-else-if="isError">Failed to load scopes.</p>
      <FilterListItem v-else v-for="product in products" :key="product.id" :query-key="'scopeId'" :value="product.id.toString()" :label="product.name"/>
    </div>
</template>