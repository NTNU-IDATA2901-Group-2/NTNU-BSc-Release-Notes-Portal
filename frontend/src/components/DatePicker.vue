<script setup lang="ts">
import type { DateValue } from '@internationalized/date'
import { CalendarDate, DateFormatter, getLocalTimeZone, today } from '@internationalized/date'

import { CalendarIcon } from '@lucide/vue'
import { cn } from '@/utils/utils'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { inject, onMounted, ref, watch, type Ref } from 'vue'

const defaultPlaceholder = today(getLocalTimeZone())
const date = ref() as Ref<DateValue | undefined>

const df = new DateFormatter('en-US', {
  dateStyle: 'long',
})

const props = defineProps<{
  placeholder: string
  queryKey: string
}>()

const searchParams = inject('searchParams') as Ref<{ [key: string]: string }>;

onMounted(() => {
  const paramDate = searchParams.value[props.queryKey];
  if (paramDate) {
    const jsDate = new Date(paramDate);
    date.value = new CalendarDate(jsDate.getFullYear(), jsDate.getMonth() + 1, jsDate.getDate());
  }
})

watch(date, (newDate) => {
  if (newDate) {
    searchParams.value[props.queryKey] = newDate.toString();
  } else {
    delete searchParams.value[props.queryKey];
  }
})

watch(searchParams, () => {
  const paramDate = searchParams.value[props.queryKey];
  if (paramDate) {
    const jsDate = new Date(paramDate);
    date.value = new CalendarDate(jsDate.getFullYear(), jsDate.getMonth() + 1, jsDate.getDate());
  } else {
    date.value = undefined;
  }
}, { deep: true });

</script>

<template>
  <Popover v-slot="{ close }">
    <PopoverTrigger as-child>
      <Button
        variant="outline"
        :class="cn('w-50 justify-start text-left font-normal')"
      >
        <CalendarIcon />
        {{ date ? df.format(date.toDate(getLocalTimeZone())) : props.placeholder }}
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-auto p-0" align="start">
      <Calendar
        v-model="date"
        :default-placeholder="defaultPlaceholder"
        layout="month-and-year"
        initial-focus
        @update:model-value="close"
      />
    </PopoverContent>
  </Popover>
</template>
