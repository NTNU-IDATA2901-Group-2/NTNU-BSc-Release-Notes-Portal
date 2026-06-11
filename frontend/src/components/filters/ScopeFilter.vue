<script lang="ts" setup>
import { useGetScopes } from '@/api/scopes-api';
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';

const selected = defineModel<string[]>('selected', { default: () => [] });
const includeUnassigned = defineModel<boolean>('includeUnassigned', { default: false });

const { data: scopes, isLoading, isError } = useGetScopes();
const { t } = useI18n();

const toggle = (id: string, checked: boolean) => {
  selected.value = checked
    ? [...selected.value, id]
    : selected.value.filter((value) => value !== id);
};
</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.scope') }}</h4>
      <p v-if="isLoading">{{ t('loading.filter') }}</p>
      <p v-else-if="isError">{{ t('loadingError.scopes') }}</p>
      <div v-else class="flex gap-3 flex-col">
        <FilterListItem v-model="includeUnassigned" :label="t('filters.none')" />
        <FilterListItem
          v-for="scope in scopes"
          :key="scope.id"
          :model-value="selected.includes(scope.id.toString())"
          :label="scope.name"
          @update:model-value="(checked) => toggle(scope.id.toString(), checked)"
        />
      </div>
    </div>
</template>
