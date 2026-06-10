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
import { Drawer, DrawerContent, DrawerTrigger } from '@/components/ui/drawer';
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
import { isAdmin } from '@/utils/keycloak';
import AllocatedFilter from '@/components/filters/AllocatedFilter.vue';
import DateRangeFilter from '@/components/filters/DateRangeFilter.vue';
import Pagination from '@/components/ui/pagination/Pagination.vue';
import PaginationContent from '@/components/ui/pagination/PaginationContent.vue';
import PaginationPrevious from '@/components/ui/pagination/PaginationPrevious.vue';
import PaginationItem from '@/components/ui/pagination/PaginationItem.vue';
import PaginationNext from '@/components/ui/pagination/PaginationNext.vue';
import NativeSelectOption from '@/components/ui/native-select/NativeSelectOption.vue';
import NativeSelect from '@/components/ui/native-select/NativeSelect.vue';

const router = useRouter();
const { t } = useI18n();

const getInitialSelections = () => {
  const url = new URL(globalThis.location.href);
  return Object.fromEntries(url.searchParams.entries());
}

const initialSelections = getInitialSelections();
const { selection, ...initialSearchParams } = initialSelections;
const searchParams = ref(initialSearchParams);

const pageIndex = ref(initialSearchParams.page ? parseInt(initialSearchParams.page) : 0);
const pageSize = ref(initialSearchParams.size ? parseInt(initialSearchParams.size) : 10);
const pageSizeOptions = [10, 20, 50, 100];
watch([pageIndex, pageSize], () => {
  searchParams.value = { ...searchParams.value, page: (pageIndex.value - 1).toString(), size: pageSize.value.toString() };
}, { deep: true });

const { isLoading, isFetching, isError, data } = useGetChangeNotes(searchParams);

const initialSelection = selection ?? '';
const selectedChangeNotes = ref<number[]>(initialSelection.split(',').map(id => Number.parseInt(id)).filter(id => !Number.isNaN(id)));
const selectionString = computed(() => selectedChangeNotes.value.join(','));

watch([searchParams, selectionString], () => {
  let queryParams = { ...searchParams.value };

  if (selectedChangeNotes.value.length > 0) {
    queryParams.selection = selectionString.value;
  }

  router.replace({ query: queryParams });
}, { deep: true });

provide('searchParams', searchParams);

const search = ref<string>(searchParams.value.query || '');

const clearFilters = () => {
  searchParams.value = {};
}

const isChangeNoteSelected = (changeNoteId: number) => {
  return selectedChangeNotes.value.includes(changeNoteId);
}

const toggleSelection = (changeNote: ChangeNote) => {
  if (isChangeNoteSelected(changeNote.id)) {
    selectedChangeNotes.value = selectedChangeNotes.value.filter(id => id !== changeNote.id);
  } else {
    selectedChangeNotes.value.push(changeNote.id);
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
  publishChangeNoteMutation({ ids: selectedChangeNotes.value, publish: true });
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

</script>

<template>
  <Drawer>
    <DrawerContent>
      <ScrollArea class="h-100 p-5">
        <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters')
        }}</Button>
        <AllocatedFilter v-if="isAdmin" />
        <PublicPrivateFilter v-if="isAdmin"/>
        <ProductFilter />
        <ScopeFilter />
        <FeatureFilter />
        <CustomerFilter v-if="isAdmin" />
        <DateRangeFilter />
      </ScrollArea>
    </DrawerContent>
    <div class="max-h-screen w-full flex justify-center align-bottom mt-6">
      <div class="flex gap-8 flex-col min-h-full w-full md:flex-row justify-center p-4">
        <ScrollArea class="h-[80vh] hidden md:block">
          <h1 class="text-3xl text-nowrap">{{ t('title.changeNotes') }}</h1>
          <AllocatedFilter v-if="isAdmin" />
          <PublicPrivateFilter v-if="isAdmin"/>
          <ProductFilter />
          <ScopeFilter />
          <FeatureFilter />
          <CustomerFilter v-if="isAdmin" />
          <DateRangeFilter />
          <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters') }}</Button>
        </ScrollArea>

        <div class="flex flex-col w-full gap-4 max-w-4xl">
          <div class="w-full flex flex-col md:flex-row-reverse justify-center md:justify-end gap-2">
            <div class="flex justify-center gap-2 flex-wrap md:flex-nowrap" v-if="isAdmin">
              <Button variant="outline" :disabled="selectedChangeNotes.length == 0" @click="handlePublish">
                {{ t('button.publish') }}
                <Eye />
              </Button>
              <Button 
                variant="outline"
                @click="createReleaseNoteMutation.mutate(selectedChangeNotes)">
                {{ t('button.createReleaseNote') }}
                <FilePlus />
              </Button>
              <Button variant="solidaccent" @click="createChangeNoteMutation.mutate">
                {{ t('button.createChangeNote') }}
                <LayersPlus />
              </Button>
            </div>

            <div class="flex gap-2 w-full">
              <InputGroup>
                <InputGroupInput 
                  :placeholder="t('button.search')" @keyup.enter="onSearch"
                  v-model="search" />
                <Button class="ml-2">
                  <Search />
                </Button>
              </InputGroup>
              <DrawerTrigger as-child>
                <Button variant="outline" class="flex md:hidden">
                  <p>{{ t('button.filter') }}</p>
                  <ListFilterPlus />
                </Button>
              </DrawerTrigger>
            </div>
          </div>

          <div v-if="isAdmin">
            <MultiselectChangeNotes v-model="selectedChangeNotes" />
          </div>

          <Spinner v-if="isLoading || isFetching" />
          <p v-else-if="isError">{{ t('loadingError.releaseNotes') }}</p>

          <div v-else>
            <ScrollArea class="h-[60vh] w-full">
              <p v-if="data?.content.length === 0" class="text-center">{{ t('placeholder.noChangeNotesFound') }}</p>
              <div v-for="changeNote in data?.content" :key="changeNote.id" class="flex flex-col">
                <ChangeNoteCard
                  class="my-4" :key="changeNote.id"
                  :model-value="isChangeNoteSelected(changeNote.id)" :change-note="changeNote"
                  @update:model-value="toggleSelection(changeNote)" />
                <Separator />
              </div>
            </ScrollArea>
            <Pagination class="text-text-primary h-[10vh]" v-slot="{ page }" :items-per-page="pageSize" :total="data?.totalItems" :default-page="1">
              <PaginationContent v-slot="{ items }">
                <PaginationPrevious />
                <template v-for="(item, index) in items" :key="index">
                  <PaginationItem
                    v-if="item.type === 'page'"
                    :value="item.value"
                    :is-active="item.value === page"
                    @click="pageIndex = item.value"
                  >
                    {{ item.value }}
                  </PaginationItem>
                </template>
                <PaginationNext />
                <NativeSelect v-model="pageSize">
                  <NativeSelectOption
                    v-for="option in pageSizeOptions"
                    :value="option"
                    :key="option"
                    @click="pageIndex = 1"
                  >
                    {{ t('pagination.itemsPerPage', { count: option }) }}
                  </NativeSelectOption>
                </NativeSelect>
              </PaginationContent>
            </Pagination>
          </div>
        </div>
      </div>
    </div>
  </Drawer>
</template>