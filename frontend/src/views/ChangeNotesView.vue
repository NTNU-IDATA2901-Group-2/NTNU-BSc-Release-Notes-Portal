<script setup lang="ts">
import { useGetChangeNotes, useCreateChangeNote, usePublishChangeNotes } from '@/api/change-note-api';
import { useCreateReleaseNote } from '@/api/release-note-api';
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
import { Eye, FilePlus, LayersPlus, ListFilterPlus, Search } from 'lucide-vue-next';
import { computed, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { toast } from 'vue-sonner';

const router = useRouter();
const url = new URL(window.location.href);
const searchParams = ref({ ...Object.fromEntries(url.searchParams.entries()) });
const urlSearchParams = computed(() => new URLSearchParams(searchParams.value));

const { isLoading, isFetching, isError, data } = useGetChangeNotes(searchParams);

const search = ref<string>(urlSearchParams.value.get('query') || '');

watch(searchParams, () => {
  const url = new URL(window.location.href);
  url.search = urlSearchParams.value.toString();
  router.replace({ query: searchParams.value });
}, { deep: true });

provide('searchParams', searchParams);

const clearFilters = () => {
  searchParams.value = {};
}

const selectedChangeNotes = ref<ChangeNote[]>([]);
const { t } = useI18n();

const isChangeNoteSelected = (changeNote: ChangeNote) => {
  return selectedChangeNotes.value.some(selected => selected.id === changeNote.id);
}

const toggleSelection = (changeNote: ChangeNote) => {
  if (isChangeNoteSelected(changeNote)) {
    selectedChangeNotes.value = selectedChangeNotes.value.filter(note => note.id !== changeNote.id);
  } else {
    selectedChangeNotes.value.push(changeNote);
  }
}


// Publish selected changenotes
const { mutate: publishChangeNoteMutation } = usePublishChangeNotes([], true, {
  onSuccess: () => {
    toast.success(t('toast.selectedChangeNotesPublished'));
  },
  onError: () => {
    toast.error(t('toast.selectedChangeNotesPublishError'));
  },

})

const handlePublish = () => {
  console.log('Publish button clicked. Selected change note IDs:', selectedChangeNotes.value);
  publishChangeNoteMutation({ ids: selectedChangeNotes.value.map(cn => cn.id), publish: true });
  selectedChangeNotes.value = [];
}

// Create change note
const createChangeNoteMutation = useCreateChangeNote({
    onSuccess: (data?: string) => {
      router.push(`/change-notes/${data}?edit=true`);
      toast.success(t('toast.changeNoteCreated'));
    },
    onError: () => {
      toast.error(t('toast.changeNoteCreateError'));
    }
})

// Creation of release note
const createReleaseNoteMutation = useCreateReleaseNote({
  onSuccess: (data?: string) => {
    router.push(`/release-notes/${data}?edit=true`);
    toast.success(t('toast.releaseNoteCreated'));
  },
  onError: () => {
    toast.error(t('toast.releaseNoteCreateError'))
  }
})

const onSearch = () => {
  searchParams.value = { ...searchParams.value, query: search.value };
}

const selectedChangeNoteIds = computed<number[]>(() => 
  selectedChangeNotes.value.map((cn: ChangeNote) => cn.id)
);

</script>

<template>
  <div class="min-h-screen flex justify-center align-bottom mt-6">
    <div class="flex gap-8 flex-col h-min w-full md:flex-row justify-center p-4">
      <div class="h-min hidden md:block">
        <h1 class="text-2xl text-nowrap">Change Notes</h1>
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
            <Button variant="outline" @click="createChangeNoteMutation.mutate">
              {{ t('button.createChangeNote') }}
              <LayersPlus />
            </Button>
            <Button variant="solidaccent" @click="createReleaseNoteMutation.mutate(selectedChangeNoteIds)">
              {{ t('button.createReleaseNote') }}
              <FilePlus />
            </Button>
          </div>

          <div class="flex gap-2 w-full">
            <InputGroup>
              <InputGroupInput :placeholder="t('button.search')" @keyup.enter="onSearch" v-model="search"/>
              <Button class="ml-2">
                <Search />
              </Button>
            </InputGroup>
            <Button variant="outline" class="flex md:hidden">
              <p>{{ t('button.filter') }}</p>
              <ListFilterPlus />
            </Button>
          </div>
        </div>

        <div>
          <MultiselectChangeNotes v-model="selectedChangeNotes"/>
        </div>

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