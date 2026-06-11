<script lang="ts" setup>
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';
import { useGetFeatures } from '@/api/features-api';

const selected = defineModel<string[]>('selected', { default: () => [] });
const includeUnassigned = defineModel<boolean>('includeUnassigned', { default: false });

const { data: features, isLoading, isError } = useGetFeatures();
const { t } = useI18n();

const toggle = (id: string, checked: boolean) => {
  selected.value = checked
    ? [...selected.value, id]
    : selected.value.filter((value) => value !== id);
};
</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.feature') }}</h4>
      <p v-if="isLoading">{{ t('loading.filter') }}</p>
      <p v-else-if="isError">{{ t('loadingError.features') }}</p>
      <div v-else class="flex gap-3 flex-col">
        <FilterListItem v-model="includeUnassigned" :label="t('filters.none')" />
        <FilterListItem
          v-for="feature in features"
          :key="feature.id"
          :model-value="selected.includes(feature.id.toString())"
          :label="feature.name"
          @update:model-value="(checked) => toggle(feature.id.toString(), checked)"
        />
      </div>
    </div>
</template>
