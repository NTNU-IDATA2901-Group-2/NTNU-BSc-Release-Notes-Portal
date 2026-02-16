<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';
import Separator from '@/components/ui/separator/Separator.vue';
import Badge from '@/components/ui/badge/Badge.vue';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useReleaseNote } from '@/api/release-note-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';

import { Pencil, Trash2, Eye, FileDown, Ban, Save, ArrowLeft, EllipsisVertical } from "lucide-vue-next"
import { ref } from 'vue';
import Input from '@/components/ui/input/Input.vue';
import { Textarea } from '@/components/ui/textarea';
import MultiselectChangeNotes from '@/components/MultiselectChangeNotes.vue';

  const isEditing = ref(false)

  const route = useRoute();

  const id = route.params.id as string;
  const { isPending, isFetching, isError, data: releaseNote } = useReleaseNote(id);
</script>

<template>
  <main class="flex flex-col items-center px-4 mb-20">
    <Button variant="outline" class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10" @click="$router.back()"><ArrowLeft />Previous</Button>
    <div class="md:hidden flex w-full mt-4 justify-end gap-2">
      <Button v-if="isEditing" variant="outline" @click="isEditing = false">Cancel <Ban /></Button>
      <Button disabled v-if="isEditing" variant="outline" >Save <Save /></Button>
    </div>
    <Spinner v-if="isPending || isFetching" />
    <h1 v-if="isError">Error retreiving release note</h1>

    <div v-if="!isPending && !isFetching && !isError && releaseNote" class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <h1 v-if="!isEditing" class="text-2xl max-w-60 whitespace-nowrap overflow-hidden">{{ releaseNote.version }}</h1>
            <Input v-if="isEditing" class="w-full" v-model="releaseNote.version"/>
            <Badge v-if="!isEditing" class="h-6" :variant="releaseNote.published ? 'success' : 'destructive'">{{ releaseNote.published ? 'Published' : 'Private' }}</Badge>
          </div>
          <div class="flex gap-4">
          	<DropdownMenu v-if="!isEditing">
              <DropdownMenuTrigger class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                <EllipsisVertical class="text-text-primary"/>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                  <DropdownMenuItem @click="isEditing = !isEditing">
                    <div class="w-full flex gap-2">
                        <p class="text-text-dark-static ml-auto">Edit</p>
                        <Pencil class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-dark-static">Delete</p>
                        <Trash2 class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-dark-static">{{ releaseNote.published ? 'Publish' : 'Unpublish' }}</p>
                        <Eye class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-dark-static">Export</p>
                        <FileDown class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
              </DropdownMenuContent>
	          </DropdownMenu>
            <Button class="hidden md:flex" v-if="isEditing" variant="outline" @click="isEditing = false">Cancel <Ban /></Button>
          	<Button class="hidden md:flex" disabled v-if="isEditing" variant="outline" >Save <Save /></Button>
          </div>
          </div>

          <p v-if="!isEditing" class="">{{ releaseNote.description }}</p>
          <Textarea v-if="isEditing" class="w-full" v-model="releaseNote.description"/>
      </div>
      <Separator class="w-full h-2"/>
      <div class="flex flex-col w-full text-xl gap-10">
      <div class="flex flex-col w-full text-xl gap-10">
        <h2>Change Notes</h2>
        <MultiselectChangeNotes v-if="isEditing" :change-notes="releaseNote.changeNotes"/>
        <div
            v-if="!isEditing"
            v-for="change in releaseNote.changeNotes"
            :key="change.id" 
            class="flex flex-col gap-4"
            >
          <h3 class="text-lg">{{ change.reference }}</h3>
          <div>
            <h3 class="text-lg">Description</h3>
            <p class="text-sm">{{ change.description }}</p>
          </div>
          <div>
            <h3 class="text-lg">Developer Notes</h3>
            <p class="text-sm">{{ change.developerNotes }}</p>
          </div>
          <div>
            <h3 class="text-lg">Upgrade Notes</h3>
            <p class="text-sm">{{ change.upgradeNotes }}</p>
          </div>
        </div>
      </div>
      </div>
    </div>
  </main>
</template>