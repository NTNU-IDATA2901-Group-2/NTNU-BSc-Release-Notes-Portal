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
import { ref } from 'vue';

const props = defineProps<PrimitiveProps & {
    mode: SelectMode,
    selectedId?: Number
}>()

type SelectMode = 'product' | 'scope' | 'feature' | 'customer'

const hookMap = {
    product: useProducts,
    scope: useScopes,
    feature: useFeatures,
    customer: useCustomers
} as const

const { isLoading, isError, data: tags } = hookMap[props.mode]()

</script>


<template>
    <Select>
    <SelectTrigger class="w-[180px]">
        <SelectValue placeholder="Select a feature" />
    </SelectTrigger>
    <SelectContent>
        <SelectGroup>
        <SelectItem value="none" class="text-text-primary/50">
            None
        </SelectItem>
        <SelectItem v-for="tag in tags" :value="tag.id">
            {{ tag.name }}
        </SelectItem>
        </SelectGroup>
    </SelectContent>
    </Select>
</template>