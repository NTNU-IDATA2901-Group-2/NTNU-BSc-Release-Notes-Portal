<script lang="tsx" setup>
import { useI18n } from 'vue-i18n';
import SingleFilterListItem from './SingleFilterListItem.vue';

const data = {
    published: [
        { val: 'true', name: 'Published' },
    ],
    private: [
        { val: 'false', name: 'Private' },
    ]
}
const { t } = useI18n();

</script>

<template>
    <div class="flex gap-3 flex-col mt-4">
      <h4 class="text-xl">{{ t('title.status') }}</h4>
        <SingleFilterListItem v-for="option in data.published" :key="option.val" :query-key="'published'" :value="option.val" :label="option.name"/>
        <SingleFilterListItem v-for="option in data.private" :key="option.val" :query-key="'published'" :value="option.val" :label="option.name"/>
    </div>
</template>