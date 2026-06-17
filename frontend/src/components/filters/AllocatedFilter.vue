<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import FilterListItem from './FilterListItem.vue';

const active = defineModel<boolean>({ default: false });

const { t } = useI18n();
</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.allocated') }}</h4>
        <FilterListItem v-model="active" :label="'Unallocated'" />
    </div>
</template>
