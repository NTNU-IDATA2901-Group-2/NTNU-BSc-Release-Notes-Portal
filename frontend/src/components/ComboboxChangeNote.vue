<script setup lang="ts">
import { ChevronDown } from 'lucide-vue-next'
import {
  ListboxContent,
  ListboxFilter,
  ListboxItem,
  ListboxRoot,
} from 'reka-ui'
import { computed, ref, useTemplateRef } from 'vue'
import { refDebounced, unrefElement, useInfiniteScroll } from '@vueuse/core'
import { Button } from '@/components/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { useGetChangeNotesInfinite } from '@/api/change-note-api'
import { useI18n } from 'vue-i18n'
import { getLabelFromChangeNote } from '@/utils/change-note'
import type { ChangeNote } from '@/utils/types'

const { t } = useI18n();

const props = defineProps<{
  placeholder: string;
  disabled?: boolean;
  searchParams?: Record<string, string>;
}>()

const model = defineModel<ChangeNote | null>({ required: true, default: null })

const searchTerm = ref('')
const debouncedSearch = refDebounced(searchTerm, 300)
const params = computed(() => ({ ...props.searchParams, query: debouncedSearch.value }))

const {
  data: availableChangeNotes,
  fetchNextPage,
  hasNextPage,
  isFetchingNextPage,
} = useGetChangeNotesInfinite(params, () => !props.disabled)

const changeNoteOptions = computed(() =>
  availableChangeNotes.value?.pages.flatMap((page) => page.content) ?? []
)

const CLEAR_VALUE = -1

const selectedId = computed({
  get: () => model.value?.id ?? CLEAR_VALUE,
  set: (id: number) => {
    model.value = changeNoteOptions.value.find((cn) => cn.id === id) ?? null
  },
})

const selectedLabel = computed(() => model.value === null ? null : getLabelFromChangeNote(model.value))

const open = ref(false)

const onSelect = () => {
  searchTerm.value = ''
  open.value = false
}

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
    <ListboxRoot v-model="selectedId" highlight-on-hover>
      <PopoverTrigger as-child>
        <Button type="button" variant="outline" class="w-45 justify-between font-normal" :disabled="props.disabled">
          <span :class="`truncate ${model === null ? 'text-text-primary/60' : ''}`">
            {{ selectedLabel ?? (model === null ? props.placeholder : t('placeholder.noTitle')) }}
          </span>
          <ChevronDown class="size-3.5 shrink-0" />
        </Button>
      </PopoverTrigger>

      <PopoverContent
        class="w-[var(--reka-popper-anchor-width)] min-w-45 p-1 border border-border"
        @open-auto-focus.prevent
      >
        <ListboxFilter
          v-model="searchTerm"
          :placeholder="t('placeholder.search')"
          class="w-full px-2 py-1.5 text-sm bg-transparent outline-hidden placeholder:text-text-primary/60"
        />
        <p v-if="changeNoteOptions.length === 0" class="px-2 py-1.5 text-sm text-text-primary/50">
          {{ t('placeholder.noResults') }}
        </p>
        <ListboxContent
          v-else
          ref="listboxContent"
          class="max-h-75 scroll-py-1 overflow-x-hidden overflow-y-auto"
          tabindex="0"
        >
          <ListboxItem
            :value="CLEAR_VALUE"
            class="min-w-0 truncate text-text-primary/50 data-highlighted:bg-border/25 relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none"
            @select="onSelect"
          >
            {{ props.placeholder }}
          </ListboxItem>
          <ListboxItem
            v-for="changeNote in changeNoteOptions"
            :key="changeNote.id"
            :value="changeNote.id"
            class="min-w-0 truncate text-text-primary data-highlighted:bg-border/25 relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-disabled:pointer-events-none data-disabled:opacity-50"
            @select="onSelect"
          >
            <span :class="`truncate ${getLabelFromChangeNote(changeNote) === null ? 'text-text-primary/50' : ''}`">
              {{ getLabelFromChangeNote(changeNote) ?? t('placeholder.noTitle') }}
            </span>
          </ListboxItem>
        </ListboxContent>
      </PopoverContent>
    </ListboxRoot>
  </Popover>
</template>
