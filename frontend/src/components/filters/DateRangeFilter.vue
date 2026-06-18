<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import DatePicker from '../DatePicker.vue';
import { Accordion } from '../ui/accordion';
import { AccordionContent, AccordionItem, AccordionTrigger } from 'reka-ui';
import { ChevronDown } from 'lucide-vue-next';

const from = defineModel<string | undefined>('from');
const to = defineModel<string | undefined>('to');

const { t } = useI18n();
</script>

<template>
  <div class="flex gap-3 flex-col mt-4">
    <Accordion type="single" collapsible>
      <AccordionItem value="dateRange">
        <AccordionTrigger class="flex items-center justify-between w-full">
          <h4 class="text-xl hover:underline">{{ t('filters.dateRange') }}</h4>
          <ChevronDown class="text-text-primary accordion-chevron" />
        </AccordionTrigger>
        <AccordionContent class="mt-4 accordion-content">
          <div class="flex flex-col gap-3">
            <DatePicker v-model="from" :placeholder="t('filters.startRange')" :max="to" />
            <DatePicker v-model="to" :placeholder="t('filters.endRange')" :min="from" />
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  </div>
</template>
