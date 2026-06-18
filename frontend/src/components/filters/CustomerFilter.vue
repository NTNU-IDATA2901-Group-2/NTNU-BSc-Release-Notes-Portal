<script lang="ts" setup>
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';
import { useGetCustomers } from '@/api/customers-api';
import { Accordion } from '../ui/accordion';
import { AccordionContent, AccordionItem, AccordionTrigger } from 'reka-ui';
import { ChevronDown } from 'lucide-vue-next';

const selected = defineModel<string[]>('selected', { default: () => [] });
const includeUnassigned = defineModel<boolean>('includeUnassigned', { default: false });

const { data: customers, isLoading, isError } = useGetCustomers();
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
      <AccordionItem value="customer">
        <AccordionTrigger class="flex items-center justify-between w-full">
          <h4 class="text-xl hover:underline">{{ t('title.customer') }}</h4>
          <ChevronDown class="text-text-primary accordion-chevron" />
        </AccordionTrigger>
        <AccordionContent class="mt-4 accordion-content">
          <p v-if="isLoading">{{ t('loading.filter') }}</p>
          <p v-else-if="isError">{{ t('loadingError.customers') }}</p>
          <div v-else class="flex gap-3 flex-col">
            <FilterListItem v-model="includeUnassigned" :label="t('filters.none')" />
            <FilterListItem v-for="customer in customers" :key="customer.id"
              :model-value="selected.includes(customer.id.toString())" :label="customer.name"
              @update:model-value="(checked) => toggle(customer.id.toString(), checked)" />
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  </div>
</template>