<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import DatePicker from '../DatePicker.vue';

const { t } = useI18n();
</script>

<template>
  <div class="flex gap-3 flex-col mt-4">
    <h4 class="text-xl">{{ t('filters.dateRange') }}</h4>
    <div class="flex flex-col gap-3">
      <DatePicker :placeholder="t('filters.startRange')" :query-key="'fromDate'" :max-query-key="'toDate'" />
      <DatePicker :placeholder="t('filters.endRange')" :query-key="'toDate'" :min-query-key="'fromDate'" />
    </div>
  </div>
</template>