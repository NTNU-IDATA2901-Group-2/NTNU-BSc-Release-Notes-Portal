<script setup lang="ts">
import { useGetCustomers } from '@/api/customers-api';
import { useGetFeatures } from '@/api/features-api';
import { useGetProducts } from '@/api/products-api';
import { useGetReleaseNotesInfinite } from '@/api/release-note-api';
import { useGetScopes } from '@/api/scopes-api';
import type { Tag } from '@/utils/types';
import { Button } from './ui/button';
import type { PrimitiveProps } from 'reka-ui';
import { computed, ref, useTemplateRef } from 'vue';
import { unrefElement, useInfiniteScroll } from '@vueuse/core';
import { useI18n } from 'vue-i18n';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { cn } from '@/utils/utils';
import { ChevronsUpDownIcon } from 'lucide-vue-next';

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
  releaseNote: useGetReleaseNotesInfinite
} as const

const searchParams = computed(() => props.searchParams ?? {})

const { data, fetchNextPage, hasNextPage, isFetchingNextPage } =
  hookMap[props.mode](searchParams) as ReturnType<typeof useGetReleaseNotesInfinite>

const commandListRef = useTemplateRef('commandList')

useInfiniteScroll(
  () => unrefElement(commandListRef),
  async () => {
    if (props.mode === 'releaseNote') await fetchNextPage()
  },
  {
    distance: 80,
    canLoadMore: () =>
      props.mode === 'releaseNote' && hasNextPage.value && !isFetchingNextPage.value,
  }
)

const tags = computed<Tag[]>(() => {
  const value = data.value
  if (!value) return []
  const items: Array<{ id: number; name?: string; tag?: string }> =
    Array.isArray(value) ? value : value.pages.flatMap(page => page.content)
  return items.map(item => ({ id: item.id, name: item.name ?? item.tag ?? '' }))
})

const getTagFromId = (id?: number) => {
  if (id === undefined || id === -1) return t('button.none')
  const tag = tags.value.find(t => t.id === id)
  return tag ? tag.name : t('button.none')
}

const { t } = useI18n();

const currentValue = () => (props.modelValue ?? props.selectedId ?? -1).toString()

const open = ref(false);
</script>

<template>
  <Popover v-model:open="open">
    <PopoverTrigger as-child>
      <Button variant="outline" role="combobox" :aria-expanded="open" class="w-[200px] justify-between">
        {{
          currentValue() !== '-1'
            ? getTagFromId(Number(currentValue()))
            : t('placeholder.select')
        }}
        <ChevronsUpDownIcon class="ml-2 h-4 w-4 shrink-0 opacity-50" />
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-[200px] p-0">
      <Command>
        <CommandInput :placeholder="t('placeholder.search')" />
        <CommandList ref="commandList">
          <CommandEmpty>{{ t('placeholder.noResults') }}</CommandEmpty>
          <CommandGroup>
            <CommandItem :key="-1" :value="-1" @select="() => {
              modelValue = -1;
              emit('update:modelValue', -1)
              open = false
            }">
              <CheckIcon :class="cn(
                'mr-2 h-4 w-4',
                -1 === modelValue ? 'opacity-100' : 'opacity-0',
              )" />
              {{ t('button.none') }}

            </CommandItem>
            <CommandItem v-for="tag in tags" :key="tag.id" :value="tag" @select="() => {
              modelValue = tag.id;
              emit('update:modelValue', tag.id)
              open = false
            }">
              <CheckIcon :class="cn(
                'mr-2 h-4 w-4',
                tag.id === modelValue ? 'opacity-100' : 'opacity-0',
              )" />
              {{ tag.name || t('placeholder.unnamed') }}
            </CommandItem>
          </CommandGroup>
        </CommandList>
      </Command>
    </PopoverContent>
  </Popover>
</template>
