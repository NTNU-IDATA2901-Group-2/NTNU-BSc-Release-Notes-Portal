<script lang="tsx" setup>
import FilterListItem from './FilterListItem.vue';
import { useProducts } from '@/api/products-api';

const { data: products, isLoading, isError } = useProducts();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-lg">Product</h4>
      <p v-if="isLoading">Loading products...</p>
      <p v-else-if="isError">Failed to load products.</p>
      <FilterListItem v-else v-for="product in products" :key="product.id" :query-key="'productId'" :value="product.id.toString()" :label="product.name"/>
    </div>
</template>