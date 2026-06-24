<script setup lang="ts">
import { useGetCustomers } from '@/api/customers-api';
import { useGetFeatures } from '@/api/features-api';
import { useGetProducts } from '@/api/products-api';
import { useGetReleaseNotes } from '@/api/release-note-api';
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
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps<PrimitiveProps & {
    mode: SelectMode,
    selectedId?: number,
    modelValue?: number
    searchParams?: Record<string, string>
    disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

type SelectMode = 'product' | 'scope' | 'feature' | 'customer' | 'releaseNote'

const hookMap = {
    product: useGetProducts,
    scope: useGetScopes,
    feature: useGetFeatures,
    customer: useGetCustomers,
    releaseNote: useGetReleaseNotes
} as const

const searchParams = computed(() => props.searchParams ?? {})

const { data } = hookMap[props.mode](searchParams)

const tags = computed<Tag[]>(() => {
    const value = data.value
    if (!value) return []
    const items: Array<{ id: number; name?: string; tag?: string }> =
        Array.isArray(value) ? value : value.content
    return items.map(item => ({ id: item.id, name: item.name ?? item.tag ?? '' }))
})

const getTagFromId = (id?: number) => {
    if (id === undefined || id === -1) return t('button.none')
    const tag = tags.value.find(t => t.id === id)
    return tag ? tag.name : t('button.none')
}

const { t } = useI18n();

const currentValue = () => (props.modelValue ?? props.selectedId ?? -1).toString()

</script>

<template>
    <Select :model-value="currentValue()" :disabled="disabled" @update:model-value="(val) => emit('update:modelValue', val ? parseInt(val as string) : -1)">
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
