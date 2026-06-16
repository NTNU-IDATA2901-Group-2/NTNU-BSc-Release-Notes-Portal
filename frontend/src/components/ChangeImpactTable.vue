<script setup lang="ts">
import type { ChangeImpact, Feature, TestingNeed } from '@/utils/types'
import { FlexRender, getCoreRowModel, useVueTable, type ColumnDef } from '@tanstack/vue-table'
import { computed, h } from 'vue'
import Table from './ui/table/Table.vue'
import TableHeader from './ui/table/TableHeader.vue'
import TableRow from './ui/table/TableRow.vue'
import TableBody from './ui/table/TableBody.vue'
import TableCell from './ui/table/TableCell.vue'
import TableHead from './ui/table/TableHead.vue'
import { useI18n } from 'vue-i18n'
import SelectFeatures from './SelectFeatures.vue'
import SelectTestingNeed from './SelectTestingNeed.vue'
import Textarea from './ui/textarea/Textarea.vue'
import { Button } from './ui/button'
import { Plus, Trash2 } from 'lucide-vue-next'

const { t } = useI18n();

const model = defineModel<ChangeImpact[]>({
  default: () => []
})

const props = defineProps({
  editable: {
    type: Boolean,
    default: false,
    required: false
  }
})

function updateData<K extends keyof ChangeImpact>(
  rowIndex: number,
  key: K,
  value: ChangeImpact[K]
) {
  model.value = model.value.map((row, index) =>
    index === rowIndex ? { ...row, [key]: value } : row
  )
}

// Temporary ids for rows added in the UI; never sent to the backend (stripped on submit).
let nextTempId = -1;

function addChangeImpact() {
  model.value = [
    ...model.value,
    { id: nextTempId--, feature: undefined, whatIsChanged: '', whatShouldBeTested: '', testingNeed: undefined },
  ]
}

function removeChangeImpact(rowIndex: number) {
  model.value = model.value.filter((_, index) => index !== rowIndex)
}

const columns = computed<ColumnDef<ChangeImpact>[]>(() => {
  const baseColumns: ColumnDef<ChangeImpact>[] = [
  {
    accessorKey: 'feature.name',
    header: () => t('title.feature'),
    cell: ({ cell, row }) => {
      return props.editable
        ? h(SelectFeatures, {
            class:'w-full',
            'modelValue': row.original.feature,
            'onUpdate:modelValue': (value?: Feature) => updateData(row.index, 'feature', value)
          })
        : cell.getValue() as string;
    }
  },
  {
    accessorKey: 'whatIsChanged',
    header: () => t('title.whatIsChanged'),
    cell: ({ cell, row }) => {
      return props.editable
        ? h(Textarea, {
            class: 'resize-none',
            'modelValue': cell.getValue() as string,
            'onUpdate:modelValue': (value: string | number) =>
              updateData(row.index, 'whatIsChanged', String(value))
          })
        : cell.getValue() as string;
    }
  },
  {
    accessorKey: 'whatShouldBeTested',
    header: () => t('title.whatShouldBeTested'),
    cell: ({ cell, row }) => {
      return props.editable
        ? h(Textarea, {
            class: 'resize-none',
            'modelValue': cell.getValue() as string,
            'onUpdate:modelValue': (value: string | number) =>
              updateData(row.index, 'whatShouldBeTested', String(value))
          })
        : cell.getValue() as string;
    }
  },
  {
    accessorKey: 'testingNeed',
    header: () => t('title.testingNeed'),
    cell: ({ row }) => {
      const testingNeed = row.original.testingNeed;
      return props.editable
        ? h(SelectTestingNeed, {
            class: 'w-full',
            'modelValue': testingNeed,
            'onUpdate:modelValue': (value?: TestingNeed) => updateData(row.index, 'testingNeed', value)
          })
        : h('span', { class: 'capitalize' }, testingNeed ? t(`testingNeeds.${testingNeed.toLowerCase()}`) : '');
    }
  },
  ]

  if (props.editable) {
    baseColumns.push({
      id: 'actions',
      header: () => '',
      cell: ({ row }) =>
        h(Button, {
          type: 'button',
          variant: 'outline',
          size: 'icon',
          'aria-label': t('button.delete'),
          onClick: () => removeChangeImpact(row.index),
        }, () => h(Trash2)),
    })
  }

  return baseColumns
})

const table = useVueTable({
  get data() { return model.value },
  get columns() { return columns.value },
  getCoreRowModel: getCoreRowModel(),
})
</script>


<template>
  <div class="flex flex-col gap-2">
    <div class="border rounded-md overflow-hidden">
    <Table class="overflow-hidden">
      <TableHeader>
        <TableRow v-for="headerGroup in table.getHeaderGroups()" :key="headerGroup.id">
          <TableHead v-for="header in headerGroup.headers" :key="header.id" class="whitespace-normal">
            <FlexRender
              v-if="!header.isPlaceholder" :render="header.column.columnDef.header"
              :props="header.getContext()"
            />
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <template v-if="table.getRowModel().rows?.length">
          <TableRow
            v-for="row in table.getRowModel().rows" :key="row.id"
            :data-state="row.getIsSelected() ? 'selected' : undefined"
          >
            <TableCell v-for="cell in row.getVisibleCells()" :key="cell.id" class="whitespace-normal wrap-break-word">
              <FlexRender :render="cell.column.columnDef.cell" :props="cell.getContext()" />
            </TableCell>
          </TableRow>
        </template>
        <template v-else>
          <TableRow>
            <TableCell :colspan="columns.length" class="h-24 text-center">
              {{ t('placeholder.noChangeImpacts') }}
            </TableCell>
          </TableRow>
        </template>
      </TableBody>
    </Table>
    </div>
    <Button
      v-if="editable" type="button" variant="outline" class="w-fit"
      @click="addChangeImpact"
    >
      {{ t('button.addChangeImpact') }}
      <Plus />
    </Button>
  </div>
</template>
