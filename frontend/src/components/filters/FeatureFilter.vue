<script lang="tsx" setup>
import FilterListItem from './FilterListItem.vue';
import { useI18n } from 'vue-i18n';
import { useGetFeatures } from '@/api/features-api';

const { data: features, isLoading, isError } = useGetFeatures();
const { t } = useI18n();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.feature') }}</h4>
      <p v-if="isLoading">{{ t('loading.filter') }}</p>
      <p v-else-if="isError">{{ t('loadingError.features') }}</p>
      <FilterListItem v-else v-for="feature in features" :key="feature.id" :query-key="'featureIds'" :value="feature.id.toString()" :label="feature.name"/>
    </div>
</template>