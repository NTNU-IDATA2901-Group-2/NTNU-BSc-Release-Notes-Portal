<script setup lang="ts">
import type { DateValue } from '@internationalized/date'
import { DateFormatter, getLocalTimeZone, today } from '@internationalized/date'

import { CalendarIcon } from '@lucide/vue'
import { cn } from '@/utils/utils'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { getLocaleDateString } from '@/utils/format-date'

const defaultPlaceholder = today(getLocalTimeZone())
const date = defineModel<DateValue | undefined>()

defineProps<{
  placeholder: string
  min?: DateValue
  max?: DateValue
}>()
</script>

<template>
  <Popover v-slot="{ close }">
    <PopoverTrigger as-child>
      <Button
        variant="outline"
        :class="cn('w-50 justify-start text-left font-normal')"
      >
        <CalendarIcon />
        {{ date ? getLocaleDateString(date.toString()) : placeholder }}
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-auto p-0" align="start">
      <Calendar
        v-model="date"
        :default-placeholder="defaultPlaceholder"
        :min-value="min"
        :max-value="max"
        layout="month-and-year"
        initial-focus
        @update:model-value="close"
      />
    </PopoverContent>
  </Popover>
</template>
