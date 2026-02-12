<script setup lang="ts">
import { CheckIcon, ChevronDown } from 'lucide-vue-next'
import { ListboxContent, ListboxFilter, ListboxItem, ListboxItemIndicator, ListboxRoot, useFilter, type PrimitiveProps } from 'reka-ui'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { Button } from '@/components/ui/button'
import { Popover, PopoverAnchor, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { TagsInput, TagsInputInput, TagsInputItem, TagsInputItemDelete, TagsInputItemText } from '@/components/ui/tags-input'
import type { ChangeNote } from '@/types'

const changenotes = [
  { value: 'next.js', label: 'Next.js' },
  { value: 'sveltekit', label: 'SvelteKit' },
  { value: 'nuxt', label: 'Nuxt' },
  { value: 'remix', label: 'Remix' },
  { value: 'astro', label: 'Astro' },
]



const props = defineProps<PrimitiveProps & {
    changeNotes: ChangeNote[],
}>()

const searchTerm = ref('')
const changenotesRef = ref(props.changeNotes.map(cn => cn.reference))
const open = ref(false)

const { contains } = useFilter({ sensitivity: 'base' })

const filteredFrameworks = computed(() =>
  searchTerm.value === ''
    ? changenotes
    : changenotes.filter(option => contains(option.label, searchTerm.value)),
)

watch(searchTerm, (f) => {
  if (f) {
    open.value = true
  }
})

const anchorRef = ref()
const width = ref(0)  // Add this ref for reactive width

const contentWidth = computed(() => width.value ? `${width.value}px` : 'auto')  // Update to use width ref

let resizeObserver: ResizeObserver  // Add this

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
    <ListboxRoot
      v-model="changenotesRef"
      highlight-on-hover
      multiple
    >
      <PopoverAnchor ref="anchorRef" class="inline-flex w-full">
        <TagsInput v-slot="{ modelValue: tags }" v-model="changenotesRef" class="w-full">
          <TagsInputItem class="bg-border/20 dark:bg-border p-2" v-for="item in tags" :key="item.toString()" :value="item.toString()">
            <TagsInputItemText />
            <TagsInputItemDelete class="cursor-pointer" />
          </TagsInputItem>

          <ListboxFilter v-model="searchTerm" as-child>
            <TagsInputInput placeholder="Change Notes..." @keydown.enter.prevent @keydown.down="open = true" />
          </ListboxFilter>

          <PopoverTrigger as-child>
            <Button size="icon-sm" variant="ghost" class="order-last self-start ml-auto">
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
        <ListboxContent class="max-h-[300px] scroll-py-1 overflow-x-hidden overflow-y-auto empty:after:content-['No_options'] empty:p-1 empty:after:block" tabindex="0">
          <!-- <CommandEmpty>No results found.</CommandEmpty> -->
          <ListboxItem
            v-for="item in filteredFrameworks" :key="item.value" class="text-text-primary data-[highlighted]:bg-border/25 [&_svg:not([class*=\'text-\'])]:text-muted-foreground relative flex cursor-default items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-hidden select-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*=\'size-\'])]:size-4" :value="item.label" @select="() => {
              searchTerm = ''
            }"
          >
            <span>{{ item.label }}</span>

            <ListboxItemIndicator
              class="ml-auto inline-flex items-center justify-center"
            >
              <CheckIcon />
            </ListboxItemIndicator>
          </ListboxItem>
        </ListboxContent>
      </PopoverContent>
    </ListboxRoot>
  </Popover>
</template>
