<script setup lang="ts">
import type { ChangeImpact, Feature, TestingNeed } from '@/utils/types'
import { FlexRender, getCoreRowModel, useVueTable, type ColumnDef } from '@tanstack/vue-table'
import { h } from 'vue'
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

const columns: ColumnDef<ChangeImpact>[] = [
  {
    accessorKey: 'feature.name',
    header: () => t('title.feature'),
    cell: ({ cell, row }) => {
      return props.editable
        ? h(SelectFeatures, {
            class:'w-full',
            'modelValue': row.original.feature,
            'onUpdate:modelValue': (value?: Feature) => value && updateData(row.index, 'feature', value)
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

const table = useVueTable({
  get data() { return model.value },
  get columns() { return columns },
  getCoreRowModel: getCoreRowModel(),
})
</script>


<template>
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
            <TableCell v-for="cell in row.getVisibleCells()" :key="cell.id" class="whitespace-normal align-top wrap-break-word">
              <FlexRender :render="cell.column.columnDef.cell" :props="cell.getContext()" />
            </TableCell>
          </TableRow>
        </template>
        <template v-else>
          <TableRow>
            <TableCell :colspan="columns.length" class="h-24 text-center">
              {{ t('placeholder.noResults') }}
            </TableCell>
          </TableRow>
        </template>
      </TableBody>
    </Table>
  </div>
</template>
