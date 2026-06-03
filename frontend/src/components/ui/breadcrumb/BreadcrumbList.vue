<script lang="ts" setup>
import type { HTMLAttributes } from "vue"
import { cn } from "@/utils/utils"

const props = defineProps<{
  class?: HTMLAttributes["class"]
}>()
</script>

<template>
  <ol
    data-slot="breadcrumb-list"
    :class="cn('truncate text-muted-foreground flex max-w-full flex-nowrap items-center gap-1.5 text-sm break-words sm:gap-2.5', props.class)"
  >
    <slot />
  </ol>
</template>
