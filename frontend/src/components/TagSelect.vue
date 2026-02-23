<script setup lang="ts">
import { useCustomers, useFeatures, useProducts, useScopes } from '@/api/change-note-api';
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { PrimitiveProps } from 'reka-ui';

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
    product: useProducts,
    scope: useScopes,
    feature: useFeatures,
    customer: useCustomers
} as const

const { data: tags } = hookMap[props.mode]()

const getTagFromId = (id?: number) => {
    if (id === undefined || id === -1) return 'None'
    const tag = tags.value?.find(t => t.id === id)
    return tag ? tag.name : 'None'
}

const currentValue = () => (props.modelValue ?? props.selectedId ?? -1).toString()

</script>

<template>
    <Select :model-value="currentValue()" @update:model-value="(val) => emit('update:modelValue', val ? parseInt(val as string) : -1)">
    <SelectTrigger class="w-[180px]">
        <SelectValue 
        :text-value="getTagFromId(parseInt(currentValue()))"/>
    </SelectTrigger>
    <SelectContent>
        <SelectGroup>
        <SelectItem value="-1" class="text-text-primary/50">
            None
        </SelectItem>
        <SelectItem v-for="tag in tags" :key="tag.id" :value="tag.id.toString()">
            {{ tag.name }}
        </SelectItem>
        </SelectGroup>
    </SelectContent>
    </Select>
</template>