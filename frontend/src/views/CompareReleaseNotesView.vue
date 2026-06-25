<script setup lang="ts">
import TagSelect from '@/components/TagSelect.vue';
import { computed, ref, watch, type ComponentPublicInstance } from 'vue';
import { useI18n } from 'vue-i18n';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { CompareReleaseNotesSchema as CompareReleaseNotesSchema } from '@/schemas';
import Button from '@/components/ui/button/Button.vue';
import ChangeNoteList from '@/components/changeNote/ChangeNoteList.vue';
import ChangeNoteListFilters from '@/components/changeNote/ChangeNoteListFilters.vue';
import { uniqueCustomers } from '@/utils/change-note';
import { exportComparisonToPdf } from '@/utils/pdf';
import { useCompareReleaseNotes, useGetReleaseNotes } from '@/api/release-note-api';
import { useGetJiraServiceRequestKeys } from '@/api/jira-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { toast } from 'vue-sonner';
import { FileDown } from 'lucide-vue-next';

const { t } = useI18n();

const { defineField, handleSubmit } = useForm({
  validationSchema: toTypedSchema(CompareReleaseNotesSchema),
});

const [productId] = defineField('productId');
const [releaseNoteOneId] = defineField('releaseNoteOneId');
const [releaseNoteTwoId] = defineField('releaseNoteTwoId');

const noProductSelected = computed(
  () => productId.value === -1 || productId.value === undefined
)

const canCompare = computed(() => {
  const one = releaseNoteOneId.value;
  const two = releaseNoteTwoId.value;
  return one !== undefined && one !== -1
    && two !== undefined && two !== -1
    && one !== two;
})

const searchParams = computed(() =>
  noProductSelected.value ? undefined : { productIds: String(productId.value) }
)

const { data: productReleaseNotes } = useGetReleaseNotes(computed(() => searchParams.value ?? {}))

const { mutate: compare, data: comparedReleaseNotes, reset } = useCompareReleaseNotes()

const { mutateAsync: fetchServiceRequestKeys, isPending: isFetchingServiceRequests } = useGetJiraServiceRequestKeys()

const generalChangesChecked = ref(true);
const draftChangesChecked = ref(true);
const customerFilter = ref<number>(-1);

const changeNoteCustomers = computed(() =>
  uniqueCustomers((comparedReleaseNotes.value ?? []).flatMap(releaseNote => releaseNote.changeNotes)))

type ChangeNoteListInstance = InstanceType<typeof ChangeNoteList>;
const changeNoteLists = new Map<number, ChangeNoteListInstance>();
const refSetters = new Map<number, (el: Element | ComponentPublicInstance | null) => void>();

const setChangeNoteListRef = (id: number) => {
  let setter = refSetters.get(id);
  if (!setter) {
    setter = (el) => {
      if (el) changeNoteLists.set(id, el as ChangeNoteListInstance);
      else changeNoteLists.delete(id);
    };
    refSetters.set(id, setter);
  }
  return setter;
};

const handleExport = async () => {
  const releaseNotes = comparedReleaseNotes.value;
  if (!releaseNotes?.length) return;
  const mostRecent = releaseNotes[0];
  if (!mostRecent) return;
  try {
    const oldestExcludedId = mostRecent.id === releaseNoteOneId.value
      ? releaseNoteTwoId.value
      : releaseNoteOneId.value;
    const fromTag = productReleaseNotes.value?.content.find(note => note.id === oldestExcludedId)?.tag ?? '';
    const comparison = releaseNotes.map(releaseNote => ({
      releaseNote,
      changeNotes: changeNoteLists.get(releaseNote.id)?.filteredChangeNotes ?? [],
    }));
    const references = [...new Set(
      comparison.flatMap(({ changeNotes }) => changeNotes.map(note => note.reference))
        .filter(reference => reference.length > 0),
    )];
    const serviceRequestKeys = await fetchServiceRequestKeys(references);
    exportComparisonToPdf(comparison, fromTag, mostRecent.tag, serviceRequestKeys);
  } catch (error) {
    console.error('Error exporting comparison to PDF:', error);
    toast.error(t('toast.exportPdfError'));
  }
};

watch(productId, () => {
  releaseNoteOneId.value = undefined
  releaseNoteTwoId.value = undefined
  reset()
})

const onSubmit = handleSubmit((values) => {
  compare(
    { releaseNoteOneId: values.releaseNoteOneId, releaseNoteTwoId: values.releaseNoteTwoId },
    { onError: () => toast.error(t('toast.compareReleaseNotesError')) },
  )
});

</script>

<template>
  <div class="flex flex-col items-center mt-20 w-full">
    <form class="flex flex-row gap-8" @submit="onSubmit">
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.product') }}</h2>
        <TagSelect mode="product" v-model="productId" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.releaseNoteOne') }}</h2>
        <TagSelect mode="releaseNote" v-model="releaseNoteOneId" :search-params="searchParams" :disabled="noProductSelected" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.releaseNoteTwo') }}</h2>
        <TagSelect mode="releaseNote" v-model="releaseNoteTwoId" :search-params="searchParams" :disabled="noProductSelected" />
      </div>
      <Button type="submit" class="mt-auto" variant="solidaccent" :disabled="!canCompare">
        {{ t('compareReleaseNotes.compare') }}
      </Button>
    </form>

    <div
      v-if="comparedReleaseNotes && comparedReleaseNotes.length"
      class="mt-16 flex flex-col gap-16 w-full max-w-4xl px-4">
      <div class="flex flex-col-reverse gap-4 sm:flex-row sm:items-center sm:justify-between">
        <Button variant="outline" :disabled="isFetchingServiceRequests" @click="handleExport">
          <Spinner v-if="isFetchingServiceRequests" class="h-4 dark:text-text-primary" />
          <FileDown v-else /> {{ t('button.export') }}
        </Button>
        <ChangeNoteListFilters
          v-model:general-changes-checked="generalChangesChecked"
          v-model:draft-changes-checked="draftChangesChecked"
          v-model:customer-filter="customerFilter"
          :customers="changeNoteCustomers" />
      </div>
      <section v-for="releaseNote in comparedReleaseNotes" :key="releaseNote.id" class="flex flex-col gap-10">
        <h1 v-if="!releaseNote.tag" class="text-4xl text-text-primary/50 leading-normal">{{ t('placeholder.noTitle') }}</h1>
        <h1 v-else class="text-3xl md:text-4xl leading-normal">{{ releaseNote.tag }}</h1>
        <ChangeNoteList
          :ref="setChangeNoteListRef(releaseNote.id)"
          :change-notes="releaseNote.changeNotes"
          :general-changes-checked="generalChangesChecked"
          :draft-changes-checked="draftChangesChecked"
          :customer-filter="customerFilter" />
      </section>
    </div>
  </div>


</template>
