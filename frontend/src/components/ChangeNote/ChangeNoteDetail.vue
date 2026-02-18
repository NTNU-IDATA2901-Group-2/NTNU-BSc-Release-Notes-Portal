<script lang="ts" setup>
import type { ChangeNote } from '@/types';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { EllipsisVertical, Eye, FileDown, Pencil, Trash2 } from 'lucide-vue-next';

const props = defineProps<{
    changeNote: ChangeNote;
    modelValue?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>();

</script>

<template>
    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <h1 class="text-2xl max-w-60 whitespace-nowrap overflow-hidden">{{
              changeNote.reference }}</h1>
            <Badge class="h-6"
              :variant="changeNote.published ? 'success' : 'destructive'">{{ changeNote.published ?
                'Published' : 'Private' }}</Badge>
          </div>
          <div class="flex gap-4">
            <DropdownMenu>
              <DropdownMenuTrigger
                class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                <EllipsisVertical class="text-text-primary" />
              </DropdownMenuTrigger>
              <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                <DropdownMenuItem @click="emit('update:modelValue', true)">
                  <div class="w-full flex gap-2">
                    <p class="text-text-dark-static ml-auto">Edit</p>
                    <Pencil class="text-text-dark-static" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem disabled>
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-dark-static">Delete</p>
                    <Trash2 class="text-text-dark-static" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem disabled>
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-dark-static">{{ changeNote.published ? 'Publish' :
                      'Unpublish' }}</p>
                    <Eye class="text-text-dark-static" />
                  </div>
                </DropdownMenuItem>
                <DropdownMenuItem disabled>
                  <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-dark-static">Export</p>
                    <FileDown class="text-text-dark-static" />
                  </div>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <p class="">{{ changeNote.description }}</p>
        <div class="flex flex-wrap gap-4">
          <Badge v-if="changeNote.product" class="h-6">{{ changeNote.product.name }}</Badge>
          <Badge v-if="changeNote.scope" class="h-6">{{ changeNote.scope.name }}</Badge>
          <Badge v-if="changeNote.feature" class="h-6">{{ changeNote.feature.name }}</Badge>
          <Badge v-if="changeNote.customer" class="h-6">{{ changeNote.customer.name }}</Badge>
        </div>
      </div>
      <Separator class="w-full h-2" />
      <div class="flex flex-col w-full text-xl gap-10">
        <div>
          <h3 class="text-lg">Developer Notes</h3>
          <p class="text-sm">{{ changeNote.developerNotes }}</p>
          
        </div>
        <div>
          <h3 class="text-lg">Upgrade Notes</h3>
          <p class="text-sm">{{ changeNote.upgradeNotes }}</p>
        </div>
      </div>
    </div>
</template>