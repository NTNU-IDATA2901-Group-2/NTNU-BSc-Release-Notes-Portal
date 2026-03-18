<script setup lang="ts">
import { useRoute } from 'vue-router';
import { useGetReleaseNote } from '@/api/release-note-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { ref } from 'vue';
import { router } from '@/utils/router';
import { useI18n } from 'vue-i18n';
import ReleaseNoteEdit from '@/components/releaseNote/ReleaseNoteEdit.vue';
import ReleaseNoteDetail from '@/components/releaseNote/ReleaseNoteDetail.vue';

const route = useRoute();
const { t } = useI18n();
const isEditing = ref(route.query.edit === 'true');

if (route.query.edit !== undefined) {
  delete route.query.edit;
  router.replace({ query: route.query });
}

const id = route.params.id as string;

const { isPending, isFetching, isError, data: releaseNote } = useGetReleaseNote(id);

</script>

<template>
  <Spinner v-if="isPending || isFetching" />
  <p v-else-if="isError">{{ t('loadingError.releaseNotes') }}</p>
  <ReleaseNoteEdit v-else-if="releaseNote != null  && isEditing" :release-note="releaseNote" v-model:is-editing="isEditing"/>
  <ReleaseNoteDetail v-else-if="releaseNote != null" :release-note="releaseNote" v-model:is-editing="isEditing"/>
  <h1 v-else>{{ t('loadingError.releaseNotes') }}</h1>
</template>