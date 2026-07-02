<script setup lang="ts">
import TagCombobox from '@/components/TagCombobox.vue';
import { computed, ref, watch, type ComponentPublicInstance } from 'vue';
import { useI18n } from 'vue-i18n';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { DiffReleaseNotesSchema } from '@/schemas';
import Button from '@/components/ui/button/Button.vue';
import ChangeNoteList from '@/components/changeNote/ChangeNoteList.vue';
import ChangeNoteListFilters from '@/components/changeNote/ChangeNoteListFilters.vue';
import { uniqueCustomers } from '@/utils/change-note';
import { exportDiffToPdf, type PdfVariant } from '@/utils/pdf';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { useDiffReleaseNotes, useGetReleaseNotes } from '@/api/release-note-api';
import { useGetJiraServiceRequestKeys } from '@/api/jira-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { toast } from 'vue-sonner';
import { ChevronDown, FileDown } from 'lucide-vue-next';

const { t } = useI18n();

const { defineField, handleSubmit } = useForm({
  validationSchema: toTypedSchema(DiffReleaseNotesSchema),
});

const [productId] = defineField('productId');
const [releaseNoteOneId] = defineField('releaseNoteOneId');
const [releaseNoteTwoId] = defineField('releaseNoteTwoId');

const noProductSelected = computed(
  () => productId.value === -1 || productId.value === undefined
)

const canDiff = computed(() => {
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

const { mutate: diff, data: diffedReleaseNotes, reset, isPending: diffPending } = useDiffReleaseNotes()

const { mutateAsync: fetchServiceRequestKeys, isPending: isFetchingServiceRequests } = useGetJiraServiceRequestKeys()

const generalChangesChecked = ref(true);
const draftChangesChecked = ref(true);
const customerFilter = ref<number>(-1);

const changeNoteCustomers = computed(() =>
  uniqueCustomers((diffedReleaseNotes.value ?? []).flatMap(releaseNote => releaseNote.changeNotes)))

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

const handleExport = async (variants: PdfVariant[] = ['customer', 'technical']) => {
  const releaseNotes = diffedReleaseNotes.value;
  if (!releaseNotes?.length) return;
  const mostRecent = releaseNotes[0];
  if (!mostRecent) return;
  try {
    const oldestExcludedId = mostRecent.id === releaseNoteOneId.value
      ? releaseNoteTwoId.value
      : releaseNoteOneId.value;
    const fromTag = productReleaseNotes.value?.content.find(note => note.id === oldestExcludedId)?.tag ?? '';
    const diff = releaseNotes.map(releaseNote => ({
      releaseNote,
      changeNotes: changeNoteLists.get(releaseNote.id)?.selectedChangeNotes ?? [],
    }));
    const references = [...new Set(
      diff.flatMap(({ changeNotes }) => changeNotes.map(note => note.reference))
        .filter(reference => reference && reference.length > 0),
    )];
    const serviceRequestKeys = await fetchServiceRequestKeys(references);
    exportDiffToPdf(diff, fromTag, mostRecent.tag, serviceRequestKeys, variants);
  } catch (error) {
    console.error('Error exporting diff to PDF:', error);
    toast.error(t('toast.exportPdfError'));
  }
};

watch(productId, () => {
  releaseNoteOneId.value = undefined
  releaseNoteTwoId.value = undefined
  reset()
})

const onSubmit = handleSubmit((values) => {
  diff(
    { releaseNoteOneId: values.releaseNoteOneId, releaseNoteTwoId: values.releaseNoteTwoId },
    { onError: () => toast.error(t('toast.diffReleaseNotesError')) },
  )
});

</script>

<template>
  <div class="flex flex-col items-center mt-20 w-full">
    <form class="flex flex-row gap-8" @submit="onSubmit">
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('diffReleaseNotes.product') }}</h2>
        <TagCombobox mode="product" v-model="productId" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('diffReleaseNotes.releaseNoteOne') }}</h2>
        <TagCombobox mode="releaseNote" v-model="releaseNoteOneId" :search-params="searchParams"
          :disabled="noProductSelected" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('diffReleaseNotes.releaseNoteTwo') }}</h2>
        <TagCombobox mode="releaseNote" v-model="releaseNoteTwoId" :search-params="searchParams"
          :disabled="noProductSelected" />
      </div>
      <Button type="submit" class="mt-auto" variant="outline" :disabled="!canDiff">
        {{ t('diffReleaseNotes.diff') }}
        <Spinner v-if="diffPending" class="size-4 my-0" />
      </Button>
    </form>

    <div v-if="diffedReleaseNotes && diffedReleaseNotes.length"
      class="mt-16 flex flex-col gap-16 w-full max-w-4xl px-4">
      <div class="flex flex-col-reverse gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex flex-row justify-center">
          <Button variant="solidaccent" class="rounded-r-none" :disabled="isFetchingServiceRequests"
            @click="handleExport()">
            {{ t('button.export') }}
            <Spinner v-if="isFetchingServiceRequests" class="size-4 my-0" />
            <FileDown v-else />
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <Button variant="solidaccent" class="rounded-l-none" :disabled="isFetchingServiceRequests">
                <ChevronDown />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start">
              <DropdownMenuItem>
                <p class="text-text-primary">{{ t('button.exportBoth') }}</p>
              </DropdownMenuItem>
              <DropdownMenuItem @click="handleExport(['customer'])">
                <p class="text-text-primary">{{ t('button.exportCustomer') }}</p>
              </DropdownMenuItem>
              <DropdownMenuItem @click="handleExport(['technical'])">
                <p class="text-text-primary">{{ t('button.exportTechnical') }}</p>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>

        </div>
        <ChangeNoteListFilters v-model:general-changes-checked="generalChangesChecked"
          v-model:draft-changes-checked="draftChangesChecked" v-model:customer-filter="customerFilter"
          :customers="changeNoteCustomers" />
      </div>
      <section v-for="releaseNote in diffedReleaseNotes" :key="releaseNote.id" class="flex flex-col gap-10">
        <h1 v-if="!releaseNote.tag" class="text-4xl text-text-primary/50 leading-normal">{{ t('placeholder.noTitle') }}
        </h1>
        <h1 v-else class="text-3xl md:text-4xl leading-normal">{{ releaseNote.tag }}</h1>
        <ChangeNoteList :ref="setChangeNoteListRef(releaseNote.id)" :change-notes="releaseNote.changeNotes"
          :general-changes-checked="generalChangesChecked" :draft-changes-checked="draftChangesChecked"
          :customer-filter="customerFilter" />
      </section>
    </div>
  </div>


</template>
