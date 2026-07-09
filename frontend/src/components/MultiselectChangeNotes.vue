<script setup lang="ts">
import { CheckIcon, ChevronDown } from 'lucide-vue-next'
import {
  ListboxContent,
  ListboxFilter,
  ListboxItem,
  ListboxItemIndicator,
  ListboxRoot,
} from 'reka-ui'
import { computed, reactive, ref, useTemplateRef, watch, watchEffect } from 'vue'
import { refDebounced, unrefElement, useInfiniteScroll } from '@vueuse/core'
import { Button } from '@/components/ui/button'
import { Popover, PopoverAnchor, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { TagsInput, TagsInputInput, TagsInputItem, TagsInputItemDelete } from '@/components/ui/tags-input'
import { useGetChangeNotes, useGetChangeNotesInfinite } from '@/api/change-note-api'
import { useI18n } from 'vue-i18n'
import { getLabelFromChangeNote } from '@/utils/change-note'

const { t } = useI18n();

const model = defineModel<number[]>({ required: true })

const searchTerm = ref('')
const debouncedSearch = refDebounced(searchTerm, 300)
const searchParams = computed(() => ({ query: debouncedSearch.value }))

const {
  data: availableChangeNotes,
  fetchNextPage,
  hasNextPage,
  isFetchingNextPage,
} = useGetChangeNotesInfinite(searchParams)

const changeNoteOptions = computed(() =>
  (availableChangeNotes.value?.pages.flatMap((page) => page.content) ?? [])
    .map((cn) => ({ value: cn.id, label: getLabelFromChangeNote(cn) }))
)

const selectedFilter = computed(() => ({ filteredIds: model.value.join(',') }))
const { data: selectedChangeNotes } = useGetChangeNotes(selectedFilter, () => model.value.length > 0)

const referenceById = reactive(new Map<number, string>())
watchEffect(() => {
  const notes = [
    ...(availableChangeNotes.value?.pages.flatMap((page) => page.content) ?? []),
    ...(selectedChangeNotes.value?.content ?? []),
  ]
  for (const cn of notes) referenceById.set(cn.id, cn.reference)
})

const selectedTags = computed(() =>
  model.value.map((id) => ({ id, reference: referenceById.get(id) ?? '' }))
)

const open = ref(false)

watch(searchTerm, (value) => {
  if (value) open.value = true
})

const listboxContentRef = useTemplateRef('listboxContent')

useInfiniteScroll(
  () => unrefElement(listboxContentRef),
  async () => { await fetchNextPage() },
  {
    distance: 200,
    canLoadMore: () => hasNextPage.value && !isFetchingNextPage.value,
  }
)
</script>

<template>
  <Popover v-model:open="open">
    <ListboxRoot v-model="model" highlight-on-hover multiple>
      <PopoverAnchor class="inline-flex w-full">
        <TagsInput v-model="model" class="w-full" @click="open = true">
          <TagsInputItem
            v-for="tag in selectedTags"
            :key="tag.id"
            :value="tag.id"
            class="bg-border/20 dark:bg-border p-2"
          >
            <span class="py-0.5 px-2 text-sm rounded bg-transparent">{{ tag.reference }}</span>
            <TagsInputItemDelete @click.stop class="cursor-pointer" />
          </TagsInputItem>

          <ListboxFilter v-model="searchTerm" as-child>
            <TagsInputInput
              :placeholder="t('input.addChangeNotes')"
              @keydown.enter.prevent
              @keydown.down="open = true"
            />
          </ListboxFilter>

          <PopoverTrigger as-child>
            <Button @click.stop size="icon-sm" variant="ghost" class="order-last self-start ml-auto">
              <ChevronDown class="size-3.5" />
            </Button>
          </PopoverTrigger>
        </TagsInput>
      </PopoverAnchor>

      <PopoverContent
        class="w-[var(--reka-popper-anchor-width)] p-1 border border-border"
        @open-auto-focus.prevent
      >
        <ListboxContent
          ref="listboxContent"
          class="max-h-75 scroll-py-1 overflow-x-hidden overflow-y-auto empty:after:content-['No_options'] empty:p-1 empty:after:block"
          tabindex="0"
        >
          <ListboxItem
            v-for="item in changeNoteOptions"
            :key="item.value"
            :value="item.value"
            class="min-w-0 truncate text-text-primary data-highlighted:bg-border/25 [&_svg:not([class*='text-'])]:text-muted-foreground relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"
            @select="() => { searchTerm = '' }"
          >
            <span :class="`truncate ${item.label === null ? 'text-text-primary/50' : ''}`">
              {{ item.label !== null ? item.label : t('placeholder.noTitle') }}
            </span>
            <ListboxItemIndicator class="ml-auto inline-flex items-center justify-center">
              <CheckIcon />
            </ListboxItemIndicator>
          </ListboxItem>
        </ListboxContent>
      </PopoverContent>
    </ListboxRoot>
  </Popover>
</template>
