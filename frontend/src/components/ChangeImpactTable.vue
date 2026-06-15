<script setup lang="ts">
import type { ChangeImpact } from '@/utils/types'
import { FlexRender, getCoreRowModel, useVueTable, type ColumnDef } from '@tanstack/vue-table'
import { h } from 'vue'
import Table from './ui/table/Table.vue'
import TableHeader from './ui/table/TableHeader.vue'
import TableRow from './ui/table/TableRow.vue'
import TableBody from './ui/table/TableBody.vue'
import TableCell from './ui/table/TableCell.vue'
import TableHead from './ui/table/TableHead.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n();

const columns: ColumnDef<ChangeImpact>[] = [
  {
    accessorKey: 'feature.name',
    header: () => t('title.feature'),
  },
  {
    accessorKey: 'whatIsChanged',
    header: () => t('title.whatIsChanged'),
  },
  {
    accessorKey: 'whatShouldBeTested',
    header: () => t('title.whatShouldBeTested'),
  },
  {
    accessorKey: 'testingNeed',
    header: () => t('title.testingNeed'),
    cell: ({ getValue }) => {
      const testingNeed = getValue() as string;
      return h('span', { class: 'capitalize' }, t(`testingNeeds.${testingNeed.toLowerCase()}`));
    }
  },
]

const props = defineProps<{
  data: ChangeImpact[]
}>()

const table = useVueTable({
  get data() { return props.data },
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