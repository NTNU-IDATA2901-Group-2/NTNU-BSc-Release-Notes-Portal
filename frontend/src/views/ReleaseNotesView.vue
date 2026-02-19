<script setup lang="ts">
import { useReleaseNotes } from '@/api/release-note-api';
import ReleaseNoteCard from '@/components/ReleaseNoteCard.vue';
import Button from '@/components/ui/button/Button.vue';
import { InputGroup, InputGroupInput } from '@/components/ui/input-group';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { ListFilterPlus, Search } from 'lucide-vue-next';
import { ref } from 'vue';

const { isLoading, isFetching, isError, data } = useReleaseNotes();

const selectedItems = ref<number[]>([]);

</script>

<template>
  <div class="min-h-screen flex justify-center align-bottom">
    <div v-if="isLoading || isFetching">
      <Spinner />
    </div>

    <p v-else-if="isError">Failed to load change notes. Please try again later.</p>

    <div v-else class="flex gap-8 flex-col h-min w-full md:flex-row justify-center p-4">
      <div class="h-min hidden md:block">
        <h1 class="text-2xl text-nowrap">Release Notes</h1>
      </div>

      <div class="flex flex-col w-full gap-4 max-w-4xl">
        <div class="w-full flex flex-col md:flex-row-reverse justify-center md:justify-end gap-2">

          <div class="flex gap-2 w-full">
            <InputGroup>
              <InputGroupInput placeholder="Search" disabled/>
              <Button class="ml-2" disabled>
                <Search />
              </Button>
            </InputGroup>
            <Button variant="outline" class="flex md:hidden" disabled>
              <p>Filter</p>
              <ListFilterPlus />
            </Button>
          </div>
        </div>

        <div v-for="releaseNote in data" class="flex flex-col gap-4">
          <ReleaseNoteCard :key="releaseNote.id" :selected="selectedItems.includes(releaseNote.id)"
            :releaseNote="releaseNote" />
          <Separator />
        </div>
      </div>
    </div>
  </div>
</template>