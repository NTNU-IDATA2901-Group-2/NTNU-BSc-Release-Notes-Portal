<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import FilterListItem from './FilterListItem.vue';
import { Accordion } from '../ui/accordion';
import { AccordionContent, AccordionItem, AccordionTrigger } from 'reka-ui';
import { ChevronDown } from 'lucide-vue-next';

const published = defineModel<string | undefined>();

const options = {
    published: [
        { val: 'true', name: 'Published' },
    ],
    draft: [
        { val: 'false', name: 'Draft' },
    ]
}
const { t } = useI18n();

const set = (val: string, checked: boolean) => {
  published.value = checked ? val : undefined;
};
</script>

<template>
  <div class="flex gap-3 flex-col mt-4">
    <Accordion type="single" collapsible>
      <AccordionItem value="status">
        <AccordionTrigger class="flex items-center justify-between w-full">
          <h4 class="text-xl hover:underline">{{ t('title.status') }}</h4>
          <ChevronDown class="text-text-primary accordion-chevron" />
        </AccordionTrigger>
        <AccordionContent class="mt-4 accordion-content">
          <div class="flex gap-3 flex-col">
            <FilterListItem
              v-for="option in options.published" :key="option.val"
              :model-value="published === option.val" :label="option.name"
              @update:model-value="(checked) => set(option.val, checked)" />
            <FilterListItem
              v-for="option in options.draft" :key="option.val"
              :model-value="published === option.val" :label="option.name"
              @update:model-value="(checked) => set(option.val, checked)" />
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  </div>
</template>
