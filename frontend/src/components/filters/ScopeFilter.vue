<script lang="tsx" setup>
import { useGetScopes } from '@/api/scopes-api';
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';

const { data: scopes, isLoading, isError } = useGetScopes();
const { t } = useI18n();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-lg">{{ t('title.scope') }}</h4>
      <p v-if="isLoading">{{ t('loading.filter') }}</p>
      <p v-else-if="isError">{{ t('loadingError.scopes') }}</p>
      <FilterListItem v-else v-for="scope in scopes" :key="scope.id" :query-key="'scopeId'" :value="scope.id.toString()" :label="scope.name"/>
    </div>
</template>