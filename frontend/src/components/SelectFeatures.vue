<script setup lang="ts">
import { useGetFeatures } from '@/api/features-api';
import Select from './ui/select/Select.vue';
import SelectTrigger from './ui/select/SelectTrigger.vue';
import SelectValue from './ui/select/SelectValue.vue';
import SelectItem from './ui/select/SelectItem.vue';
import SelectContent from './ui/select/SelectContent.vue';
import { useI18n } from 'vue-i18n';
import type { HTMLAttributes } from 'vue';
import type { Feature } from '@/utils/types.ts';

const { t } = useI18n();

const props = defineProps<{ class?: HTMLAttributes['class'] }>();

const selected = defineModel<Feature | undefined>();

const { data: features, isLoading, isError } = useGetFeatures();
</script>

<template>
  <Select v-model="selected">
    <SelectTrigger :class="props.class">
      <SelectValue :placeholder="t('title.feature')">
        {{ selected?.name ?? t('title.feature') }}
      </SelectValue>
    </SelectTrigger>
    <SelectContent>
      <template v-if="!isLoading && !isError">
        <SelectItem :key="undefined" :value="null">
          {{ t('filters.none') }}
        </SelectItem>
        <SelectItem v-for="feature in features" :key="feature.id" :value="feature">
          {{ feature.name }}
        </SelectItem>
      </template>
      <p v-else-if="isError">{{ t('loadingError.features') }}</p>
      <p v-else-if="isLoading">{{ t('loading.features') }}</p>
    </SelectContent>
  </Select>
</template>
