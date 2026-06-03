<script setup lang="ts">
import { CheckIcon, ChevronDown } from 'lucide-vue-next'
import {
  ListboxContent,
  ListboxFilter,
  ListboxItem,
  ListboxItemIndicator,
  ListboxRoot,
  useFilter,
} from 'reka-ui'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { Button } from '@/components/ui/button'
import { Popover, PopoverAnchor, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { TagsInput, TagsInputInput, TagsInputItem, TagsInputItemDelete } from '@/components/ui/tags-input'
import { useGetChangeNotes } from '@/api/change-note-api'
import { useI18n } from 'vue-i18n'
import { getLabelFromChangeNote } from '@/utils/change-note'

const { t } = useI18n();

const model = defineModel<number[]>({ required: true })

const params = computed(() => { return { filteredIds : model.value.join(',') } })
const { data: selectedChangeNotes } = useGetChangeNotes(params)

const { data: availableChangeNotes } = useGetChangeNotes()

const changeNoteOptions = computed(() =>
  (availableChangeNotes.value ?? []).map((cn) => ({ value: cn.id, label: getLabelFromChangeNote(cn) }))
)


const searchTerm = ref('')
const open = ref(false)
const { contains } = useFilter({ sensitivity: 'base' })

const filteredChangenotes = computed(() =>
  searchTerm.value === ''
    ? changeNoteOptions.value
    : changeNoteOptions.value.filter((option) => contains(option.label ?? '', searchTerm.value)),
)

watch(searchTerm, (value) => {
  if (value) open.value = true
})

// Resizing
const anchorRef = ref()
const width = ref(0)
const contentWidth = computed(() => (width.value ? `${width.value}px` : 'auto'))

let resizeObserver: ResizeObserver

onMounted(() => {
  const el = anchorRef.value?.$el as HTMLElement
  if (el) {
    resizeObserver = new ResizeObserver(() => {
      width.value = el.clientWidth
    })
    resizeObserver.observe(el)
    width.value = el.clientWidth
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
})
</script>

<template>
  <Popover v-model:open="open">
    <ListboxRoot v-model="model" highlight-on-hover multiple>
      <PopoverAnchor ref="anchorRef" class="inline-flex w-full">
        <TagsInput v-model="model" class="w-full" @click="open = true">
          <TagsInputItem
            v-for="changeNote in selectedChangeNotes"
            :key="changeNote.id"
            :value="changeNote.id"
            class="bg-border/20 dark:bg-border p-2"
          >
            <span class="py-0.5 px-2 text-sm rounded bg-transparent">{{ changeNote.reference }}</span>
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
        class="p-1 border border-border"
        :style="`width: ${contentWidth} !important`"
        @open-auto-focus.prevent
      >
        <ListboxContent
          class="max-h-75 scroll-py-1 overflow-x-hidden overflow-y-auto empty:after:content-['No_options'] empty:p-1 empty:after:block"
          tabindex="0"
        >
          <ListboxItem
            v-for="item in filteredChangenotes"
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