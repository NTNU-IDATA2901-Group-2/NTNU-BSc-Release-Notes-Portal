<script setup lang="ts">
import { type ChangeNote } from '@/utils/types';
import Select from './ui/select/Select.vue';
import SelectContent from './ui/select/SelectContent.vue';
import SelectGroup from './ui/select/SelectGroup.vue';
import SelectItem from './ui/select/SelectItem.vue';
import SelectTrigger from './ui/select/SelectTrigger.vue';
import SelectValue from './ui/select/SelectValue.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<{
  changeNotes: ChangeNote[];
  disabled?: boolean;
  placeholder: string;
}>()

const model = defineModel<ChangeNote | null>({ required: true, default: null })

</script>

<template>
  <Select :disabled="props.disabled || props.changeNotes === undefined || props.changeNotes.length === 0" v-model="model">
    <SelectTrigger class="w-45">
      <SelectValue/>
    </SelectTrigger>
    <SelectContent>
      <SelectGroup>
        <SelectItem :value="null">
          {{ props.placeholder }}
        </SelectItem>
        <SelectItem v-for="changeNote in changeNotes" :key="changeNote.id" :value=changeNote>
          {{ changeNote.reference ?? t('changeNote.noReference') }}
        </SelectItem>
      </SelectGroup>
    </SelectContent>
  </Select>
</template>