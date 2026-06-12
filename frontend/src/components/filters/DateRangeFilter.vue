<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import DatePicker from '../DatePicker.vue';

const from = defineModel<string | undefined>('from');
const to = defineModel<string | undefined>('to');

const { t } = useI18n();
</script>

<template>
  <div class="flex gap-3 flex-col mt-4">
    <h4 class="text-xl">{{ t('filters.dateRange') }}</h4>
    <div class="flex flex-col gap-3">
      <DatePicker v-model="from" :placeholder="t('filters.startRange')" :max="to" />
      <DatePicker v-model="to" :placeholder="t('filters.endRange')" :min="from" />
    </div>
  </div>
</template>
