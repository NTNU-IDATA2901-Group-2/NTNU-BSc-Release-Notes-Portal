<script lang="tsx" setup>
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';
import { useGetProducts } from '@/api/products-api';

const { data: products, isLoading, isError } = useGetProducts();
const { t } = useI18n();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.product') }}</h4>
      <p v-if="isLoading">{{ t('loading.filter') }}</p>
      <p v-else-if="isError">{{ t('loadingError.products') }}</p>
      <FilterListItem v-else v-for="product in products" :key="product.id" :query-key="'productIds'" :value="product.id.toString()" :label="product.name"/>
    </div>
</template>