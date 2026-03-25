<script lang="tsx" setup>
import { useI18n } from 'vue-i18n';
import SingleFilterListItem from './SingleFilterListItem.vue';

const { t } = useI18n();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.allocated') }}</h4>
        <SingleFilterListItem :key="'allocated'" :query-key="'hasReleaseNote'" :value="'false'" :label="'Unallocated'" :initial-state="true"/>
    </div>
</template>