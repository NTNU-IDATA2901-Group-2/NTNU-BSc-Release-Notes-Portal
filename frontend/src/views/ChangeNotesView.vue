<script setup lang="ts">
import { createChangeNoteMutation, createPublishChangeNoteMutation, useChangeNotes } from '@/api/change-note-api';
import { createReleaseNoteMutation } from '@/api/release-note-api';
import ChangeNoteCard from '@/components/ChangeNoteCard.vue';
import Button from '@/components/ui/button/Button.vue';
import { InputGroup, InputGroupInput } from '@/components/ui/input-group';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { TagsInput, TagsInputItemDelete, TagsInputItemText } from '@/components/ui/tags-input';
import TagsInputItem from '@/components/ui/tags-input/TagsInputItem.vue';
import { Eye, FilePlus, LayersPlus, ListFilterPlus, Search } from 'lucide-vue-next';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const { isLoading, isFetching, isError, data } = useChangeNotes();

const selectedItems = ref<number[]>([]);

const getTitleById = (id: number): string => {
  const changeNote = data.value?.find(note => note.id === id);
  return changeNote ? changeNote.reference : `Item-${id}`;
}

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

    <div v-else class="flex gap-8 flex-col h-min w-full md:flex-row justify-center p-4">
      <div class="h-min hidden md:block">
        <h1 class="text-2xl text-nowrap">Change Notes</h1>
      </div>

      <div class="flex flex-col w-full gap-4 max-w-4xl">
        <div class="w-full flex flex-col md:flex-row-reverse justify-center md:justify-end gap-2">
          <div class="flex justify-center gap-2 flex-wrap md:flex-nowrap">
            <Button variant="outline" @click="handlePublish">Publish
              <Eye />
            </Button>
            <Button variant="outline" @click="handleCreateChangeNote">Create Change Note
              <LayersPlus />
            </Button>
            <Button variant="solidaccent" @click="handleCreateReleaseNote">Create Release Note
              <FilePlus />
            </Button>
          </div>

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

        <div>
          <TagsInput disabled>
            <TagsInputItem v-for="id in selectedItems" :key="id" :value="getTitleById(id)" readonly>
              <TagsInputItemText />
              <TagsInputItemDelete disabled/>
            </TagsInputItem>

            <TagsInputItem v-if="selectedItems.length === 0" value="No items selected" readonly class="end">
              <TagsInputItemText />
            </TagsInputItem>
          </TagsInput>
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