<script setup lang="ts">
import { type ChangeNote } from '@/utils/types';
import Select from './ui/select/Select.vue';
import SelectContent from './ui/select/SelectContent.vue';
import SelectGroup from './ui/select/SelectGroup.vue';
import SelectItem from './ui/select/SelectItem.vue';
import SelectTrigger from './ui/select/SelectTrigger.vue';
import SelectValue from './ui/select/SelectValue.vue';
import { useGetChangeNotes } from '@/api/change-note-api';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const model = defineModel<ChangeNote | null>({ required: true, default: null })

const { data: availableChangeNotes } = useGetChangeNotes()

</script>

<template>
  <Select v-model="model">
    <SelectTrigger class="w-45">
      <SelectValue placeholder="Select a fruit" />
    </SelectTrigger>
    <SelectContent>
      <SelectGroup>
        <SelectItem :value="null">
          {{ t('none') }}
        </SelectItem>
        <SelectItem v-for="changeNote in availableChangeNotes" :key="changeNote.id" :value=changeNote>
          {{ changeNote.reference }}
        </SelectItem>
      </SelectGroup>
    </SelectContent>
  </Select>
</template>