<script setup lang="ts">
import { useCreateReleaseNote, useGetReleaseNotes } from '@/api/release-note-api';
import ReleaseNoteCard from '@/components/ReleaseNoteCard.vue';
import Button from '@/components/ui/button/Button.vue';
import { InputGroup, InputGroupInput } from '@/components/ui/input-group';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { FilePlus, ListFilterPlus, Search } from 'lucide-vue-next';
import { ref, watch } from 'vue';
import { useSearchParams } from '@/composables/useSearchParams';
import { useI18n } from 'vue-i18n';
import ScrollArea from '@/components/ui/scroll-area/ScrollArea.vue';
import ProductFilter from '@/components/filters/ProductFilter.vue';
import PublishedDraftFilter from '@/components/filters/PublishedDraftFilter.vue';
import { useRouter } from 'vue-router';
import { Drawer, DrawerContent, DrawerTrigger } from '@/components/ui/drawer';
import { isAdmin } from '@/utils/auth';
import { toast } from 'vue-sonner';
import DateRangeFilter from '@/components/filters/DateRangeFilter.vue';
import Pagination from '@/components/ui/pagination/Pagination.vue';
import PaginationContent from '@/components/ui/pagination/PaginationContent.vue';
import PaginationPrevious from '@/components/ui/pagination/PaginationPrevious.vue';
import PaginationItem from '@/components/ui/pagination/PaginationItem.vue';
import PaginationNext from '@/components/ui/pagination/PaginationNext.vue';
import NativeSelectOption from '@/components/ui/native-select/NativeSelectOption.vue';
import NativeSelect from '@/components/ui/native-select/NativeSelect.vue';

const { t } = useI18n();

const selectedItems = ref<number[]>([]);

const router = useRouter();
const { params, single, csv, match, clear } = useSearchParams(router);

const productIds = csv('productIds');
const includeUnassignedProduct = match('includeUnassignedProduct', 'true');
const published = single('published');
const fromDate = single('fromDate');
const toDate = single('toDate');

const pageSizeOptions = [10, 20, 50, 100];
const defaultPage = 1;
const defaultPageSize = 10;
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

const { data, isLoading, isFetching, isError } = useGetReleaseNotes(params);
const search = ref(params.value.query || '');

const clearFilters = () => {
  clear();
  updatePaginationParams(defaultPage, defaultPageSize);
  search.value = '';
}

const onSearch = () => {
  params.value = { ...params.value, query: search.value };
}

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

</script>

<template>
  <Drawer>
    <DrawerContent>
      <div class="h-min md:block p-4">
        <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters')
        }}</Button>
        <ProductFilter v-model:selected="productIds" v-model:include-unassigned="includeUnassignedProduct" />
        <PublishedDraftFilter v-model="published" />
        <DateRangeFilter v-model:from="fromDate" v-model:to="toDate" />
      </div>
    </DrawerContent>
    <div class="max-h-screen flex justify-center align-bottom mt-6 w-full">
      <div class="flex gap-8 flex-col min-h-full w-full md:flex-row justify-center p-4">
        <div class="h-min hidden md:block">
          <h1 class="text-3xl text-nowrap">{{ t('title.releaseNotes') }}</h1>
          <ProductFilter v-model:selected="productIds" v-model:include-unassigned="includeUnassignedProduct" />
          <PublishedDraftFilter v-if="isAdmin" v-model="published" />
          <DateRangeFilter v-model:from="fromDate" v-model:to="toDate" />
          <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters')
          }}</Button>
        </div>

        <div class="flex flex-col w-full gap-4 max-w-4xl">
          <div class="w-full flex flex-col md:flex-row-reverse justify-center md:justify-end gap-2">

            <div class="flex gap-2 w-full flex-wrap sm:flex-nowrap">
              <InputGroup>
                <InputGroupInput 
                  :placeholder="t('button.search')" @keyup.enter="onSearch"
                  v-model="search" />
                <Button class="ml-2">
                  <Search />
                </Button>
              </InputGroup>
              <Button
                v-if="isAdmin" 
                variant="solidaccent"
                @click="createReleaseNoteMutation.mutate([])">
                {{ t('button.createReleaseNote') }}
                <FilePlus />
              </Button>
              <DrawerTrigger as-child>
                <Button variant="outline" class="flex md:hidden">
                  <p>{{ t('button.filter') }}</p>
                  <ListFilterPlus />
                </Button>
              </DrawerTrigger>
            </div>
          </div>
          <Spinner v-if="isLoading || isFetching" class="w-full"/>
          <p v-else-if="isError">{{ t('loadingError.releaseNotes') }}</p>
          <div v-else>
            <ScrollArea class="h-[67vh] w-full">
            <p v-if="data?.content.length === 0" class="text-center">{{ t('placeholder.noReleaseNotesFound') }}</p>
            <div v-for="releaseNote in data?.content" :key="releaseNote.id" class="flex flex-col">
              <ReleaseNoteCard 
                class="my-4" :key="releaseNote.id"
                :selected="selectedItems.includes(releaseNote.id)" :release-note="releaseNote" />
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
