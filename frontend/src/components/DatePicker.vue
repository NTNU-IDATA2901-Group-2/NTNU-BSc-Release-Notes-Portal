<script setup lang="ts">
import type { DateValue } from '@internationalized/date'
import { CalendarDate, getLocalTimeZone, today } from '@internationalized/date'

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

const model = defineModel<string | undefined>()

defineProps<{
  placeholder: string
  min?: string
  max?: string
}>()

const toCalendarDate = (value: string | undefined): CalendarDate | undefined => {
  if (!value) return undefined
  const date = new Date(value)
  return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

const onUpdate = (value: DateValue | undefined) => {
  model.value = value?.toString()
}
</script>

<template>
  <Popover v-slot="{ close }">
    <PopoverTrigger as-child>
      <Button
        variant="outline"
        :class="cn('w-50 justify-start text-left font-normal')"
      >
        <CalendarIcon />
        {{ model ? getLocaleDateString(model) : placeholder }}
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-auto p-0" align="start">
      <Calendar
        :model-value="toCalendarDate(model)"
        :default-placeholder="defaultPlaceholder"
        :min-value="toCalendarDate(min)"
        :max-value="toCalendarDate(max)"
        layout="month-and-year"
        initial-focus
        @update:model-value="(value) => { onUpdate(value); close() }"
      />
    </PopoverContent>
  </Popover>
</template>
