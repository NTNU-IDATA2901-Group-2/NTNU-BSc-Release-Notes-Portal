<script setup lang="ts">
import { useGetChangeNotes, useCreateChangeNote, usePublishChangeNotes } from '@/api/change-note-api';
import { useCreateReleaseNote } from '@/api/release-note-api';
import ChangeNoteCard from '@/components/ChangeNoteCard.vue';
import CustomerFilter from '@/components/filters/CustomerFilter.vue';
import FeatureFilter from '@/components/filters/FeatureFilter.vue';
import ProductFilter from '@/components/filters/ProductFilter.vue';
import PublishedDraftFilter from '@/components/filters/PublishedDraftFilter.vue';
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
import { computed, ref, watch } from 'vue';
import { useSearchParams } from '@/composables/useSearchParams';
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


const initialSelection = new URL(globalThis.location.href).searchParams.get('selection') ?? '';
const selectedChangeNotes = ref<number[]>(initialSelection.split(',').map(id => Number.parseInt(id)).filter(id => !Number.isNaN(id)));
const selectionString = computed(() => selectedChangeNotes.value.join(','));

const { params, single, csv, match, clear } = useSearchParams(router, {
  exclude: ['selection'],
  extraQuery: () => {
    const extra: Record<string, string> = {};
    if (selectedChangeNotes.value.length > 0) {
      extra.selection = selectionString.value;
    }
    return extra;
  },
});

const productIds = csv('productIds');
const includeUnassignedProduct = match('includeUnassignedProduct', 'true');
const scopeIds = csv('scopeIds');
const includeUnassignedScope = match('includeUnassignedScope', 'true');
const featureIds = csv('featureIds');
const includeUnassignedFeature = match('includeUnassignedFeature', 'true');
const customerIds = csv('customerIds');
const includeUnassignedCustomer = match('includeUnassignedCustomer', 'true');
const published = single('published');
const hasReleaseNote = match('hasReleaseNote', 'false');
const fromDate = single('fromDate');
const toDate = single('toDate');

// Preserve the AllocatedFilter default: "unallocated" is pre-selected for admins.
if (isAdmin.value && params.value.hasReleaseNote === undefined) {
  hasReleaseNote.value = true;
}

const defaultPage = 1;
const defaultPageSize = 10;
const pageSizeOptions = [10, 20, 50, 100];
// <Pagination> is 1-based; the backend `page` query param is 0-based.
const page = ref(params.value.page ? parseInt(params.value.page) + 1 : defaultPage);
const pageSize = ref(params.value.size ? parseInt(params.value.size) : defaultPageSize);
const updatePaginationParams = (newPage: number, newPageSize: number) => {
  page.value = newPage;
  pageSize.value = newPageSize;
  params.value = { ...params.value, page: (page.value - 1).toString(), size: pageSize.value.toString() };
};
updatePaginationParams(page.value, pageSize.value);
watch([page, pageSize], ([newPage, newSize]) => updatePaginationParams(newPage, newSize));

const { isLoading, isFetching, isError, data } = useGetChangeNotes(params);

const search = ref<string>(params.value.query || '');

const clearFilters = () => {
  clear();
  updatePaginationParams(defaultPage, defaultPageSize);
  search.value = '';
  // Preserve the AllocatedFilter default: "unallocated" is pre-selected for admins.
  if (isAdmin.value) hasReleaseNote.value = true;
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
  params.value = { ...params.value, query: search.value };
}

</script>

<template>
  <Drawer>
    <DrawerContent>
      <ScrollArea class="h-100 p-5">
        <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters')
        }}</Button>
        <AllocatedFilter v-if="isAdmin" v-model="hasReleaseNote" />
        <PublishedDraftFilter v-if="isAdmin" v-model="published" />
        <ProductFilter v-model:selected="productIds" v-model:include-unassigned="includeUnassignedProduct" />
        <ScopeFilter v-model:selected="scopeIds" v-model:include-unassigned="includeUnassignedScope" />
        <FeatureFilter v-model:selected="featureIds" v-model:include-unassigned="includeUnassignedFeature" />
        <CustomerFilter v-if="isAdmin" v-model:selected="customerIds" v-model:include-unassigned="includeUnassignedCustomer" />
        <DateRangeFilter v-model:from="fromDate" v-model:to="toDate" />
      </ScrollArea>
    </DrawerContent>
    <div class="max-h-screen w-full flex justify-center align-bottom mt-6">
      <div class="flex gap-8 flex-col min-h-full w-full md:flex-row justify-center p-4">
        <ScrollArea class="h-[80vh] hidden md:block">
          <h1 class="text-3xl text-nowrap">{{ t('title.changeNotes') }}</h1>
          <AllocatedFilter v-if="isAdmin" v-model="hasReleaseNote" />
          <PublishedDraftFilter v-if="isAdmin" v-model="published" />
          <ProductFilter v-model:selected="productIds" v-model:include-unassigned="includeUnassignedProduct" />
          <ScopeFilter v-model:selected="scopeIds" v-model:include-unassigned="includeUnassignedScope" />
          <FeatureFilter v-model:selected="featureIds" v-model:include-unassigned="includeUnassignedFeature" />
          <CustomerFilter v-if="isAdmin" v-model:selected="customerIds" v-model:include-unassigned="includeUnassignedCustomer" />
          <DateRangeFilter v-model:from="fromDate" v-model:to="toDate" />
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

          <Spinner v-if="isLoading || isFetching" class="w-full"/>
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
            <Pagination class="text-text-primary h-[10vh]" v-model:page="page" :items-per-page="pageSize" :total="data?.totalItems">
              <PaginationContent v-slot="{ items }">
                <PaginationPrevious />
                <template v-for="(item, index) in items" :key="index">
                  <PaginationItem
                    v-if="item.type === 'page'"
                    :value="item.value"
                    :is-active="item.value === page"
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
                    @click="page = 1"
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