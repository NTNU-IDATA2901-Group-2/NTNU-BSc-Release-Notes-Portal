<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { Customer } from '@/utils/types';
import Checkbox from '../ui/checkbox/Checkbox.vue';
import Select from '../ui/select/Select.vue';
import SelectTrigger from '../ui/select/SelectTrigger.vue';
import SelectValue from '../ui/select/SelectValue.vue';
import SelectContent from '../ui/select/SelectContent.vue';
import SelectGroup from '../ui/select/SelectGroup.vue';
import SelectItem from '../ui/select/SelectItem.vue';

defineProps<{
  customers: Customer[];
}>();

const generalChangesChecked = defineModel<boolean>('generalChangesChecked', { default: true });
const draftChangesChecked = defineModel<boolean>('draftChangesChecked', { default: true });
const customerFilter = defineModel<number>('customerFilter', { default: -1 });

const { t } = useI18n();
</script>

<template>
  <div class="flex flex-col sm:flex-row items-center gap-4">
    <div class="flex gap-2">
      <p>{{ t('button.showGeneralChanges') }}</p>
      <Checkbox v-model="generalChangesChecked" class="cursor-pointer" />
    </div>
    <div class="flex gap-2">
      <p>{{ t('button.showDraftChanges') }}</p>
      <Checkbox v-model="draftChangesChecked" class="cursor-pointer" />
    </div>
    <div>
      <Select v-model="customerFilter">
        <SelectTrigger class="w-42">
          <SelectValue :placeholder="t('placeholder.filterByCustomer')" />
        </SelectTrigger>
        <SelectContent>
          <SelectGroup>
            <SelectItem :value=-1 class="text-text-primary/50">
              {{ t('button.allCustomers') }}
            </SelectItem>
          </SelectGroup>
          <SelectGroup>
            <SelectItem v-for="customer in customers" :key="customer.id" :value="customer.id">
              {{ customer.name }}
            </SelectItem>
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  </div>
</template>
