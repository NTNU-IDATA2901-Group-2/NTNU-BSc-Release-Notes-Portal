<script setup lang="ts">
import { getReleaseNotes } from '@/api/release-note-api';
import ReleaseNoteCard from '@/components/ReleaseNoteCard.vue';
import Button from '@/components/ui/button/Button.vue';
import { InputGroup, InputGroupInput } from '@/components/ui/input-group';
import Separator from '@/components/ui/separator/Separator.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { ListFilterPlus, Search } from 'lucide-vue-next';
import { computed, provide, ref, watch } from 'vue';
import { useQuery } from "@tanstack/vue-query";
import type { ReleaseNote } from '@/utils/types';
import { useI18n } from 'vue-i18n';
import ScrollArea from '@/components/ui/scroll-area/ScrollArea.vue';
import ProductFilter from '@/components/filters/ProductFilter.vue';
import PublicPrivateFilter from '@/components/filters/PublicPrivateFilter..vue';

const { t } = useI18n();

const selectedItems = ref<number[]>([]);

const url = new URL(window.location.href);
const searchParams = ref({ ...Object.fromEntries(url.searchParams.entries()) });
const urlSearchParams = computed(() => new URLSearchParams(searchParams.value));
const queryKey = computed(() => ['releaseNotes', urlSearchParams.value.toString()]);
const {data, isLoading, isFetching, isError} = useQuery<ReleaseNote[]>(  
{
  queryKey,
  queryFn: () => getReleaseNotes(urlSearchParams.value)
  
});
const search = ref('');

watch(searchParams, () => {
  const url = new URL(window.location.href);
  url.search = urlSearchParams.value.toString();
  window.history.pushState({}, '', url);
}, { deep: true });

provide('searchParams', searchParams);

const clearFilters = () => {
  searchParams.value = {};
}

const onSearch = () => {
  searchParams.value = { ...searchParams.value, query: search.value };
}

</script>

<template>
  <div class="min-h-screen flex justify-center align-bottom mt-6">
    <div class="flex gap-8 flex-col h-min w-full md:flex-row justify-center p-4">
      <div class="h-min hidden md:block">
        <h1 class="text-2xl text-nowrap">Release Notes</h1>
        <ProductFilter />
        <PublicPrivateFilter />
        <Button class="mt-4" variant="outline" @click="clearFilters">{{ t('button.clearFilters') }}</Button>
      </div>

      <div class="flex flex-col w-full gap-4 max-w-4xl">
        <div class="w-full flex flex-col md:flex-row-reverse justify-center md:justify-end gap-2">

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
        <Spinner v-if="isLoading || isFetching"/>
        <p v-else-if="isError">{{ t('loadingError.releaseNotes') }}</p>
        <ScrollArea class="h-[80vh] w-full" v-else>
        <div v-for="releaseNote in data" :key="releaseNote.id" class="flex flex-col">
          <ReleaseNoteCard
            class="my-4"
            :key="releaseNote.id" :selected="selectedItems.includes(releaseNote.id)"
            :release-note="releaseNote" />
          <Separator />
        </div>
        </ScrollArea>
      </div>
    </div>
  </div>
</template>