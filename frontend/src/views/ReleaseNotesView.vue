<script setup lang="ts">
import { useCreateReleaseNote, useGetReleaseNotes } from '@/api/release-note-api';
import ReleaseNoteCard from '@/components/ReleaseNoteCard.vue';
import Button from '@/components/ui/button/Button.vue';
import { InputGroup, InputGroupInput } from '@/components/ui/input-group';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { FilePlus, ListFilterPlus, Search } from 'lucide-vue-next';
import { computed, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import ScrollArea from '@/components/ui/scroll-area/ScrollArea.vue';
import ProductFilter from '@/components/filters/ProductFilter.vue';
import PublishedDraftFilter from '@/components/filters/PublishedDraftFilter.vue';
import { useRouter } from 'vue-router';
import { Drawer, DrawerContent, DrawerTrigger } from '@/components/ui/drawer';
import { isAdmin } from '@/utils/keycloak';
import { toast } from 'vue-sonner';
import DateRangeFilter from '@/components/filters/DateRangeFilter.vue';
import Pagination from '@/components/ui/pagination/Pagination.vue';
import PaginationContent from '@/components/ui/pagination/PaginationContent.vue';
import PaginationPrevious from '@/components/ui/pagination/PaginationPrevious.vue';
import PaginationItem from '@/components/ui/pagination/PaginationItem.vue';
import PaginationEllipsis from '@/components/ui/pagination/PaginationEllipsis.vue';
import PaginationNext from '@/components/ui/pagination/PaginationNext.vue';
import NativeSelectOption from '@/components/ui/native-select/NativeSelectOption.vue';
import NativeSelect from '@/components/ui/native-select/NativeSelect.vue';

const { t } = useI18n();

const selectedItems = ref<number[]>([]);

const router = useRouter();
const url = new URL(globalThis.location.href);
const searchParams = ref({ ...Object.fromEntries(url.searchParams.entries()) });
const urlSearchParams = computed(() => new URLSearchParams(searchParams.value));

const pageIndex = ref(urlSearchParams.value.get('page') ? parseInt(urlSearchParams.value.get('page') as string) : 0);
const pageSize = ref(urlSearchParams.value.get('size') ? parseInt(urlSearchParams.value.get('size') as string) : 10);
const pageSizeOptions = [10, 20, 50, 100];
watch([pageIndex, pageSize], () => {
  searchParams.value = { ...searchParams.value, page: (pageIndex.value - 1).toString(), size: pageSize.value.toString() };
}, { deep: true });

const { data, isLoading, isFetching, isError } = useGetReleaseNotes(searchParams);
const search = ref(urlSearchParams.value.get('query') || '');
watch(searchParams, () => {
  const url = new URL(globalThis.location.href);
  url.search = urlSearchParams.value.toString();
  router.replace({ query: searchParams.value });
}, { deep: true });

provide('searchParams', searchParams);

const clearFilters = () => {
  searchParams.value = {};
}

const onSearch = () => {
  searchParams.value = { ...searchParams.value, query: search.value };
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
        <ProductFilter />
        <PublishedDraftFilter />
        <DateRangeFilter />
      </div>
    </DrawerContent>
    <div class="max-h-screen flex justify-center align-bottom mt-6 w-full">
      <div class="flex gap-8 flex-col min-h-full w-full md:flex-row justify-center p-4">
        <div class="h-min hidden md:block">
          <h1 class="text-3xl text-nowrap">{{ t('title.releaseNotes') }}</h1>
          <ProductFilter />
          <PublishedDraftFilter v-if="isAdmin"/>
          <DateRangeFilter />
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
          <Spinner v-if="isLoading || isFetching" />
          <p v-else-if="isError">{{ t('loadingError.releaseNotes') }}</p>
          <div v-else>
            <ScrollArea class="h-[70vh] w-full">
            <p v-if="data?.content.length === 0" class="text-center">{{ t('placeholder.noReleaseNotesFound') }}</p>
            <div v-for="releaseNote in data?.content" :key="releaseNote.id" class="flex flex-col">
              <ReleaseNoteCard 
                class="my-4" :key="releaseNote.id"
                :selected="selectedItems.includes(releaseNote.id)" :release-note="releaseNote" />
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