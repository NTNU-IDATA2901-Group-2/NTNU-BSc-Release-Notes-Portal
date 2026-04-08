<script setup lang="ts">
import { useGetCustomers } from '@/api/customers-api';
import { useGetFeatures } from '@/api/features-api';
import { useGetProducts } from '@/api/products-api';
import { useGetScopes } from '@/api/scopes-api';
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { Tag } from '@/utils/types';
import type { PrimitiveProps } from 'reka-ui';
import { useI18n } from 'vue-i18n';

const props = defineProps<PrimitiveProps & {
    mode: SelectMode,
    selectedId?: number,
    modelValue?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

type SelectMode = 'product' | 'scope' | 'feature' | 'customer'

const hookMap = {
    product: useGetProducts,
    scope: useGetScopes,
    feature: useGetFeatures,
    customer: useGetCustomers
} as const

const { data: tags } = hookMap[props.mode]()

const getTagFromId = (id?: number) => {
    if (id === undefined || id === -1) return  t('button.none') 
    const tag: Tag | undefined = tags.value?.find(t => t.id === id)
    return tag ? tag.name :  t('button.none') 
}

const { t } = useI18n();

const currentValue = () => (props.modelValue ?? props.selectedId ?? -1).toString()

</script>

<template>
    <Select :model-value="currentValue()" @update:model-value="(val) => emit('update:modelValue', val ? parseInt(val as string) : -1)">
    <SelectTrigger class="w-45">
        <SelectValue 
        :text-value="getTagFromId(parseInt(currentValue()))"/>
    </SelectTrigger>
    <SelectContent>
        <SelectGroup>
        <SelectItem value="-1" class="text-text-primary/50">
            {{ t('button.none') }}
        </SelectItem>
        <SelectItem v-for="tag in tags" :key="tag.id" :value="tag.id.toString()">
            {{ tag.name }}
        </SelectItem>
        </SelectGroup>
    </SelectContent>
    </Select>
</template>