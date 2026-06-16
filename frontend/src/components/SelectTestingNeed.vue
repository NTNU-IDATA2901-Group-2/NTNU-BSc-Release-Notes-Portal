<script setup lang="ts">
import Select from './ui/select/Select.vue';
import SelectTrigger from './ui/select/SelectTrigger.vue';
import SelectValue from './ui/select/SelectValue.vue';
import SelectItem from './ui/select/SelectItem.vue';
import SelectContent from './ui/select/SelectContent.vue';
import { useI18n } from 'vue-i18n';
import type { HTMLAttributes } from 'vue';
import { testingNeedValues, type TestingNeed } from '@/utils/types.ts';

const { t } = useI18n();

const props = defineProps<{ class?: HTMLAttributes['class'] }>();

const selected = defineModel<TestingNeed | undefined>();
</script>

<template>
  <Select v-model="selected">
    <SelectTrigger :class="props.class">
      <SelectValue :placeholder="t('title.testingNeed')">
        {{ selected ? t(`testingNeeds.${selected.toLowerCase()}`) : t('title.testingNeed') }}
      </SelectValue>
    </SelectTrigger>
    <SelectContent>
      <SelectItem :key="undefined" :value="null">
        {{ t('filters.none') }}
      </SelectItem>
      <SelectItem v-for="need in testingNeedValues" :key="need" :value="need">
        {{ t(`testingNeeds.${need.toLowerCase()}`) }}
      </SelectItem>
    </SelectContent>
  </Select>
</template>
