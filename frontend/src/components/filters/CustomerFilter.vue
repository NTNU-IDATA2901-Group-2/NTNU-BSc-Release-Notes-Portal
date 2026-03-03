<script lang="tsx" setup>
import FilterListItem from './FilterListItem.vue';
import { useCustomers } from '@/api/customers-api';

const { data: products, isLoading, isError } = useCustomers();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-lg">Customer</h4>
      <p v-if="isLoading">Loading customers...</p>
      <p v-else-if="isError">Failed to load customers.</p>
      <FilterListItem v-else v-for="product in products" :key="product.id" :query-key="'customerId'" :value="product.id.toString()" :label="product.name"/>
    </div>
</template>