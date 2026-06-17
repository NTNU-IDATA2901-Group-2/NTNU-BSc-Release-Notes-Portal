<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { ArrowLeft } from "lucide-vue-next"
import { ref } from 'vue';
import { useGetChangeNote } from '@/api/change-note-api';
import ChangeNoteEdit from '@/components/changeNote/ChangeNoteEdit.vue';
import ChangeNoteDetail from '@/components/changeNote/ChangeNoteDetail.vue';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../components/ui/breadcrumb';
import { useI18n } from 'vue-i18n';
import { router } from '@/utils/router';
import { getLabelFromChangeNote } from '@/utils/change-note';

const route = useRoute();

const isEditing = ref(route.query.edit === 'true');

if (route.query.edit !== undefined) {
  delete route.query.edit;
  router.replace({ query: route.query });
}

const { t } = useI18n();

const id = route.params.id as string;
const { isPending, isFetching, isError, data: changeNote } = useGetChangeNote(id);

</script>

<template>
  <div class="flex flex-col items-center px-4 mb-20">
    <div class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10 flex items-center gap-4 max-w-[calc(100%-2rem)] lg:max-w-[calc(100%-5rem)]">
      <Button variant="outline" class="shrink-0" @click="$router.back()">
        <ArrowLeft />{{ t('button.previous') }}
      </Button>
      <Breadcrumb class="text-text-primary min-w-0">
        <BreadcrumbList class="min-w-0">
          <BreadcrumbItem class="shrink-0">
            <BreadcrumbLink href="/change-notes">{{ t('title.changeNotes') }}</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator class="shrink-0" />
          <BreadcrumbItem class="min-w-0">
            <span class="truncate">{{ (changeNote && getLabelFromChangeNote(changeNote)) ?? t('placeholder.noTitle') }}</span>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    </div>

    <ChangeNoteEdit
      v-if="!isPending && !isFetching && !isError && changeNote !== undefined && isEditing"
      :change-note="changeNote" v-model="isEditing" />
    <ChangeNoteDetail
      v-if="!isPending && !isFetching && !isError && changeNote !== undefined && !isEditing"
      :change-note="changeNote" v-model="isEditing" />
    <div v-else-if="isPending || isFetching">
      <Spinner />
    </div>
    <h1 v-else-if="isError">{{ t('toast.changeNoteLoadingError') }}</h1>

  </div>
</template>