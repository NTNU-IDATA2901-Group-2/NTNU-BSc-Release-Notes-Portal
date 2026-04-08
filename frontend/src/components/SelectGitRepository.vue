<script setup lang="ts">
import { type GitRepository } from '@/utils/types';
import Select from './ui/select/Select.vue';
import SelectContent from './ui/select/SelectContent.vue';
import SelectGroup from './ui/select/SelectGroup.vue';
import SelectItem from './ui/select/SelectItem.vue';
import SelectTrigger from './ui/select/SelectTrigger.vue';
import SelectValue from './ui/select/SelectValue.vue';
import { useGetGitRepositories } from '@/api/git-repository-api'; 
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const model = defineModel<GitRepository | null>({ required: true, default: null })
const { data: availableGitRepositories } = useGetGitRepositories()

</script>

<template>
  <Select v-model="model">
    <SelectTrigger class="w-45">
      <SelectValue placeholder="" />
    </SelectTrigger>
    <SelectContent>
      <SelectGroup>
        <SelectItem :value="null">
          {{ t('none') }}
        </SelectItem>
        <SelectItem v-for="gitRepository in availableGitRepositories" :key="gitRepository.id" :value=gitRepository>
          {{ gitRepository.name }}
        </SelectItem>
      </SelectGroup>
    </SelectContent>
  </Select>
</template>