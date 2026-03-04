<script setup lang="ts">
import { createChangeNote, getChangeNotes, publishChangeNote } from '@/api/change-note-api';
import { createReleaseNote } from '@/api/release-note-api';
import ChangeNoteCard from '@/components/ChangeNoteCard.vue';
import CustomerFilter from '@/components/filters/CustomerFilter.vue';
import FeatureFilter from '@/components/filters/FeatureFilter.vue';
import ProductFilter from '@/components/filters/ProductFilter.vue';
import PublicPrivateFilter from '@/components/filters/PublicPrivateFilter..vue';
import ScopeFilter from '@/components/filters/ScopeFilter.vue';
import MultiselectChangeNotes from '@/components/MultiselectChangeNotes.vue';
import Button from '@/components/ui/button/Button.vue';
import { InputGroup, InputGroupInput } from '@/components/ui/input-group';
import ScrollArea from '@/components/ui/scroll-area/ScrollArea.vue';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import type { ChangeNote } from '@/utils/types';
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query';
import { Eye, FilePlus, LayersPlus, ListFilterPlus, Search } from 'lucide-vue-next';
import { computed, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { toast } from 'vue-sonner';

const router = useRouter();
const searchParams = ref({});
const urlSearchParams = computed(() => new URLSearchParams(searchParams.value));
const queryKey = computed(() => ['changeNotes', urlSearchParams.value.toString()]);
const { isLoading, isFetching, isError, data } = useQuery({
  queryKey,
  queryFn: () => getChangeNotes(urlSearchParams.value)
});

watch(searchParams, () => {
  console.log('Search parameters updated:', searchParams.value.toString());
}, { deep: true });

provide('searchParams', searchParams);

const clearFilters = () => {
  searchParams.value = {};
}

const selectedItems = ref<ChangeNote[]>([]);
const { t } = useI18n();

const isChangeNoteSelected = (changeNote: ChangeNote) => {
  return selectedItems.value.some(selected => selected.id === changeNote.id);
}

const toggleSelection = (changeNote: ChangeNote) => {
  if (isChangeNoteSelected(changeNote)) {
    selectedItems.value = selectedItems.value.filter(note => note.id !== changeNote.id);
  } else {
    selectedItems.value.push(changeNote);
  }
}

const queryClient = useQueryClient();

// Publish selected changenotes

const publishChangeNoteMutation = useMutation({
    mutationFn: (changeNoteId: number) => publishChangeNote(changeNoteId, true),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
      toast.success(t('toast.selectedChangeNotesPublished'));
    }
});;

const handlePublish = () => {
  console.log('Publish button clicked. Selected change note IDs:', selectedItems.value);
  for (const changeNote of selectedItems.value) {
    publishChangeNoteMutation.mutate(changeNote.id);
  }
  selectedItems.value = [];
}

// Create change note

const createChangeNoteMutation = useMutation({
    mutationFn: () => createChangeNote(),
    onSuccess: (data) => {
      router.push(`/change-notes/${data}?edit=true`);
      console.log('Change Note created with ID:', data);
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
    }
});;

const handleCreateChangeNote = () => {
  console.log('Create Change Note button clicked');
  createChangeNoteMutation.mutate()
}

// Creation of release note

const createReleaseNoteMutation = useMutation({
    mutationFn: () => createReleaseNote(selectedItems.value.map(cn => cn.id)),
      onSuccess: (data) => {
      router.push(`/release-notes/${data}?edit=true`);
      console.log('Release Note created with ID:', data);
      queryClient.invalidateQueries({ queryKey: ['releaseNotes'] });
    },
    onError: (error) => {
      console.error('Error creating release note:', error);
      toast.error(error.name)
    }
})

const handleCreateReleaseNote = () => {
  console.log('Creating a Release Note with selected change notes. Selected change note IDs:', selectedItems.value);
  createReleaseNoteMutation.mutate();
}

</script>

<template>
  <div class="min-h-screen flex justify-center align-bottom mt-6">
    <div class="flex gap-8 flex-col h-min w-full md:flex-row justify-center p-4">
      <div class="h-min hidden md:block">
        <h1 class="text-2xl text-nowrap">Change Notes</h1>@
        <PublicPrivateFilter />
        <ProductFilter />
        <ScopeFilter />
        <FeatureFilter />
        <CustomerFilter />
        <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters') }}</Button>
      </div>

      <div class="flex flex-col w-full gap-4 max-w-4xl">
        <div class="w-full flex flex-col md:flex-row-reverse justify-center md:justify-end gap-2">
          <div class="flex justify-center gap-2 flex-wrap md:flex-nowrap">
            <Button variant="outline" @click="handlePublish">
              {{ t('button.publish') }}
              <Eye />
            </Button>
            <Button variant="outline" @click="handleCreateChangeNote">
              {{ t('button.createChangeNote') }}
              <LayersPlus />
            </Button>
            <Button variant="solidaccent" @click="handleCreateReleaseNote">
              {{ t('button.createReleaseNote') }}
              <FilePlus />
            </Button>
          </div>

          <div class="flex gap-2 w-full">
            <InputGroup>
              <InputGroupInput :placeholder="t('button.search')" disabled/>
              <Button class="ml-2" disabled>
                <Search />
              </Button>
            </InputGroup>
            <Button variant="outline" class="flex md:hidden" disabled>
              <p>{{ t('button.filter') }}</p>
              <ListFilterPlus />
            </Button>
          </div>
        </div>

        <MultiselectChangeNotes v-model="selectedItems"/>

        <Spinner v-if="isLoading || isFetching"/>
        <p v-else-if="isError">{{ t('loadingError.releaseNotes') }}</p>

        <ScrollArea class="h-[75vh] w-full" v-else>
        <div v-for="changeNote in data" :key="changeNote.id" class="flex flex-col">
          <ChangeNoteCard 
            class="my-4"
            :key="changeNote.id" :model-value="isChangeNoteSelected(changeNote)"
            :change-note="changeNote" @update:model-value="toggleSelection(changeNote)" />
          <Separator />
        </div>
        </ScrollArea>
      </div>
    </div>
  </div>
</template>