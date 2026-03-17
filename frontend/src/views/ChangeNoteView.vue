<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { ArrowLeft } from "lucide-vue-next"
import { ref } from 'vue';
import { useGetChangeNote } from '@/api/change-note-api';
import ChangeNoteEdit from '@/components/ChangeNote/ChangeNoteEdit.vue';
import ChangeNoteDetail from '@/components/ChangeNote/ChangeNoteDetail.vue';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../components/ui/breadcrumb';
import { useI18n } from 'vue-i18n';
import { router } from '@/utils/router';

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
    <div class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10 flex  items-center gap-4">
      <Button variant="outline" class="" @click="$router.back()">
        <ArrowLeft />{{ t('button.previous') }}
      </Button>
      <Breadcrumb class="text-text-primary">
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/change-notes">Change Notes</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            {{ changeNote?.reference }}
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