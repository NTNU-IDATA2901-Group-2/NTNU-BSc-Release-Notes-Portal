<script setup lang="ts">
import { createChangeNoteMutation, createPublishChangeNoteMutation, useChangeNotes } from '@/api/change-note-api';
import { createReleaseNoteMutation } from '@/api/release-note-api';
import ChangeNoteCard from '@/components/ChangeNoteCard.vue';
import Button from '@/components/ui/button/Button.vue';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const { isLoading, isFetching, isError, data } = useChangeNotes();

const selectedItems = ref<number[]>([]);

const toggleSelection = (id: number) => {
  if (selectedItems.value.includes(id)) {
    selectedItems.value = selectedItems.value.filter(itemId => itemId !== id);
  } else {
    selectedItems.value.push(id);
  }
}

const publishChangeNoteMutation = createPublishChangeNoteMutation();

const handlePublish = () => {
  console.log('Publish button clicked. Selected change note IDs:', selectedItems.value);
  for (const id of selectedItems.value) {
    publishChangeNoteMutation.mutate(id);
  }
}

const createChangeNoteMutationInstance = createChangeNoteMutation((data: number) => {
  router.push(`/change-notes/${data}`);
})

const handleCreateChangeNote = () => {
  console.log('Create Change Note button clicked'); 
  createChangeNoteMutationInstance.mutate()
}

const createReleaseNoteMutationInstance = createReleaseNoteMutation((data: number) => {
  router.push(`/release-notes/${data}`);
})

const handleCreateReleaseNote = () => {
  console.log('Creating a Release Note with selected change notes. Selected change note IDs:', selectedItems.value);
  createReleaseNoteMutationInstance.mutate()
}

</script>

<template>
  <div class="min-h-screen flex justify-center align-bottom">
    <div v-if="isLoading || isFetching">
      <Spinner />
    </div>

    <p v-else-if="isError">Failed to load change notes. Please try again later.</p>

    <div v-else class="flex gap-8 flex-col h-min w-full md:flex-row justify-center mt-8 ">
      <div class="p-4 h-min">
        <h1 class="text-2xl">Change Notes</h1>
      </div>

      <div class="flex flex-col w-full gap-4 max-w-3xl">
        <div class="w-full flex justify-end gap-4 flex-wrap">
          <Button variant="outline" @click="handlePublish">Publish</Button>
          <Button variant="outline" @click="handleCreateChangeNote">Create Change Note</Button>
          <Button variant="outline" @click="handleCreateReleaseNote">Create Release Note</Button>
        </div>

        <div v-for="changeNote in data" class="flex flex-col gap-4">
          <ChangeNoteCard :key="changeNote.id" :selected="selectedItems.includes(changeNote.id)"
            :changeNote="changeNote" @update:selected="toggleSelection(changeNote.id)" />
          <Separator />
        </div>

      </div>
    </div>
  </div>
</template>