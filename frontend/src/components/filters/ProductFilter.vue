<script lang="ts" setup>
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';
import { useGetProducts } from '@/api/products-api';
import { Accordion } from '../ui/accordion';
import { AccordionContent, AccordionItem, AccordionTrigger } from 'reka-ui';
import { ChevronDown } from 'lucide-vue-next';

const selected = defineModel<string[]>('selected', { default: () => [] });
const includeUnassigned = defineModel<boolean>('includeUnassigned', { default: false });

const { data: products, isLoading, isError } = useGetProducts();
const { t } = useI18n();

const toggle = (id: string, checked: boolean) => {
  selected.value = checked
    ? [...selected.value, id]
    : selected.value.filter((value) => value !== id);
};
</script>

<template>
  <div class="flex gap-3 flex-col mt-4">
    <Accordion type="single" collapsible>
      <AccordionItem value="product">
        <AccordionTrigger class="flex items-center justify-between w-full">
          <h4 class="text-xl hover:underline">{{ t('title.product') }}</h4>
          <ChevronDown class="text-text-primary accordion-chevron"/>

        </AccordionTrigger>
        <AccordionContent class="mt-4 accordion-content">
          <p v-if="isLoading">{{ t('loading.filter') }}</p>
          <p v-else-if="isError">{{ t('loadingError.products') }}</p>
          <div v-else class="flex gap-3 flex-col">
            <FilterListItem v-model="includeUnassigned" :label="t('filters.none')" />
            <FilterListItem
              v-for="product in products" :key="product.id"
              :model-value="selected.includes(product.id.toString())" :label="product.name"
              @update:model-value="(checked) => toggle(product.id.toString(), checked)" />
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  </div>
</template>
