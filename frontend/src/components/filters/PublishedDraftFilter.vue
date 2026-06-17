<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import FilterListItem from './FilterListItem.vue';

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
      <h4 class="text-xl">{{ t('title.status') }}</h4>
        <FilterListItem
          v-for="option in options.published"
          :key="option.val"
          :model-value="published === option.val"
          :label="option.name"
          @update:model-value="(checked) => set(option.val, checked)"
        />
        <FilterListItem
          v-for="option in options.draft"
          :key="option.val"
          :model-value="published === option.val"
          :label="option.name"
          @update:model-value="(checked) => set(option.val, checked)"
        />
    </div>
</template>
