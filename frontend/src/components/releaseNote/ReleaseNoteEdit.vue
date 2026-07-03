<script setup lang="ts">
import { useUpdateReleaseNote } from '@/api/release-note-api';
import { ChangeImpactSchema, EditReleaseNoteSchema } from '@/schemas';
import { type GitRepository, type ChangeNote, type ReleaseNote, type ChangeImpact } from '@/utils/types';
import { z } from 'zod';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { onBeforeUnmount, onMounted, computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue-sonner';
import { Button } from '../ui/button';
import { ArrowLeft, Ban, Plus, Save, Sparkles, Trash2 } from 'lucide-vue-next';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import MultiselectChangeNotes from '../MultiselectChangeNotes.vue';
import SelectChangeNotes from '../SelectChangeNotes.vue';
import { useGetChangeNotes, useGetHasCommits } from '@/api/change-note-api';
import { onBeforeRouteLeave } from 'vue-router';
import SelectGitRepository from '../SelectGitRepository.vue';
import { useSummarizeChangeNotes } from '@/api/ai-api';
import Tooltip from '../ui/tooltip/Tooltip.vue';
import TooltipTrigger from '../ui/tooltip/TooltipTrigger.vue';
import TooltipContent from '../ui/tooltip/TooltipContent.vue';
import DialogPrompt from '../DialogPrompt.vue';
import { useGetGitRepositories } from '@/api/git-repository-api';
import Spinner from '../ui/spinner/Spinner.vue';
import DatePicker from '../DatePicker.vue';
import ChangeImpactTable from '../ChangeImpactTable.vue';
import TagCombobox from '../TagCombobox.vue';

const { t } = useI18n();

const props = defineProps<{
  releaseNote: ReleaseNote;
}>();

const isEditing = defineModel("isEditing", { type: Boolean, required: true });
const cancelDialogOpen = ref(false);

const releaseNote = props.releaseNote;

const changeNoteIdsWithinReleaseNote = ref<number[]>(releaseNote.changeNotes?.map(cn => cn.id) || [])

const { data: availableGitRepositories } = useGetGitRepositories()
const { data: availableChangeNotesPage } = useGetChangeNotes()
const availableChangeNotes = computed(() => availableChangeNotesPage.value?.content ?? [])

const gitRepository = ref<GitRepository | null>(null)
const params = computed(() => {
  return { gitRepositoryIds: gitRepository.value?.id ? gitRepository.value?.id.toString() : '' }
});
const { data: currentGitRepositoryChangeNotesPage, isFetched: isCurrentGitRepositoryChangeNotesFetched } = useGetChangeNotes(params)
const currentGitRepositoryChangeNotes = computed(() => currentGitRepositoryChangeNotesPage.value?.content ?? [])

const fromChangeNote = ref<ChangeNote | null>(null);
const toChangeNote = ref<ChangeNote | null>(null);

const onChangeNotesUpdate = (value: number[]) => {
  changeNoteIdsWithinReleaseNote.value = value;
  fromChangeNote.value = null;
  toChangeNote.value = null;
}

const onChangeNoteRangeChange = (gitRepositoryId: number) => {
  const fromChangeNoteValue = fromChangeNote.value
  const toChangeNoteValue = toChangeNote.value
  const fromIndex = availableChangeNotes.value?.findIndex((cn) => cn.id === fromChangeNoteValue?.id) ?? -1;
  const toIndex = availableChangeNotes.value?.findIndex((cn) => cn.id === toChangeNoteValue?.id) ?? -1;

  if (gitRepositoryId !== -1) { // remove old change notes on the same repository before adding new range
    changeNoteIdsWithinReleaseNote.value = changeNoteIdsWithinReleaseNote.value.filter(id => {
      const cn = availableChangeNotes.value?.find(cn => cn.id === id)
      return cn?.gitRepositoryId !== gitRepositoryId
    })
  }

  let newChangeNoteIds: number[];
  if (fromChangeNoteValue !== null && toChangeNoteValue !== null && fromIndex !== -1 && toIndex !== -1) {
    newChangeNoteIds = availableChangeNotes.value?.map(cn => cn.id).slice(fromIndex, toIndex + 1) ?? []
  } else if (fromChangeNoteValue !== null && toChangeNoteValue === null && fromIndex !== -1) {
    newChangeNoteIds = availableChangeNotes.value?.map(cn => cn.id).slice(fromIndex) ?? []
  } else if (fromChangeNoteValue === null && toChangeNoteValue !== null && toIndex !== -1) {
    newChangeNoteIds = availableChangeNotes.value?.map(cn => cn.id).slice(0, toIndex + 1) ?? []
  } else {
    newChangeNoteIds = []
  }

  changeNoteIdsWithinReleaseNote.value = [...new Set([...(changeNoteIdsWithinReleaseNote.value ?? []), ...newChangeNoteIds])];
}

const loadingSummary = ref(false);

const summarizeReleaseNote = useSummarizeChangeNotes({
  onSuccess: (summary) => {
    if (summary === undefined) {
      toast.error(t('toast.summarizeError'));
    } else {
      form.setFieldValue('summary', summary);
      toast.success(t('toast.summarizeSuccess'));
    }
  },
  onError: () => {
    toast.error(t('toast.summarizeError'));
  },
  onSettled: () => {
    loadingSummary.value = false;
  }
})

const onSummarize = () => {
  loadingSummary.value = true;
  summarizeReleaseNote.mutate(changeNoteIdsWithinReleaseNote.value);
}

const hasCommits = useGetHasCommits(changeNoteIdsWithinReleaseNote);
const disableSummarizeButton = computed(() => hasCommits.isPending.value || hasCommits.isError.value || hasCommits.data.value !== true || loadingSummary.value);


const form = useForm({
  validationSchema: toTypedSchema(EditReleaseNoteSchema),
  initialValues: {
    tag: releaseNote.tag || '',
    summary: releaseNote.summary || '',
    changeNoteIds: changeNoteIdsWithinReleaseNote.value,
    productId: releaseNote.product?.id ?? -1,
    published: releaseNote.published,
  }
})

const onCancel = () => {
  cancelDialogOpen.value = true;
}

const cancelEdit = () => {
  isEditing.value = false;
}

const onSubmit = form.handleSubmit((values) => {
  if (releaseNote === undefined) return;

  const mappedChangeImpacts = changeImpacts.value.map(ci => ({
    featureId: ci.feature?.id,
    whatIsChanged: ci.whatIsChanged,
    whatShouldBeTested: ci.whatShouldBeTested,
    testingNeed: ci.testingNeed
  }));

  const changeImpactsResult = z.array(ChangeImpactSchema).safeParse(mappedChangeImpacts);
  if (!changeImpactsResult.success) {
    const messages = [...new Set(changeImpactsResult.error.issues.map(issue => issue.message))];
    messages.forEach(message => toast.error(t(message)));
    return;
  }

  const payload = {
    ...values,
    changeNoteIds: changeNoteIdsWithinReleaseNote.value,
    productId: values.productId === -1 ? undefined : values.productId,
    releaseTimeline: {
      previewAvailableFrom: previewAvailableFrom.value,
      recommendedTestPhaseFrom: recommendedTestPhaseFrom.value,
      recommendedTestPhaseTo: recommendedTestPhaseTo.value,
      plannedProductionDeployment: plannedProductionDeployment.value,
    },
    knownLimitations: knownLimitations.value.map(limitation => limitation.trim()).filter(limitation => limitation !== ''),
    changeImpacts: changeImpactsResult.data
  }
  updateReleaseNoteMutation.mutate({ id: releaseNote.id, dto: payload });
}, ({ errors }) => {
    console.error('Change note edit validation failed', errors);
    toast.error(t('toast.releaseNoteUpdateError'));
})

const updateReleaseNoteMutation = useUpdateReleaseNote({
  onSuccess: () => {
    toast.success(t('toast.releaseNoteUpdatedSuccess'));
    isEditing.value = false;
  },
  onError: () => {
    toast.error(t('toast.releaseNoteUpdateError'));
  }
})

const [tag] = form.defineField('tag');
const [summary] = form.defineField('summary');
const [productId] = form.defineField('productId');

// Warn user of unsaved changes when trying to leave the page
const beforeUnloadListener = (event: BeforeUnloadEvent) => {
  event.preventDefault();
  ; (event as unknown as { returnValue: string }).returnValue = ''
}

onBeforeRouteLeave(() => {
  return globalThis.confirm(t('changeNoteEdit.cancelDescription')) === true;
})

onMounted(() => {
  globalThis.addEventListener('beforeunload', beforeUnloadListener);
})

onBeforeUnmount(() => {
  globalThis.removeEventListener('beforeunload', beforeUnloadListener);
})

const releaseTimeline = releaseNote.releaseTimeline
const previewAvailableFrom = ref<string | undefined>(releaseTimeline?.previewAvailableFrom);
const recommendedTestPhaseFrom = ref<string | undefined>(releaseTimeline?.recommendedTestPhaseFrom);
const recommendedTestPhaseTo = ref<string | undefined>(releaseTimeline?.recommendedTestPhaseTo);
const plannedProductionDeployment = ref<string | undefined>(releaseTimeline?.plannedProductionDeployment);

const knownLimitations = ref<string[]>([...(releaseNote.knownLimitations ?? [])]);

const addKnownLimitation = () => {
  knownLimitations.value.push('');
}

const removeKnownLimitation = (index: number) => {
  knownLimitations.value.splice(index, 1);
}

const changeImpacts = ref<ChangeImpact[]>([...(releaseNote.changeImpacts ?? [])]);

</script>


<template>
  <DialogPrompt 
  :open="cancelDialogOpen" :mode="'confirm'"
    :title-key="'releaseNoteEdit.cancelTitle'"
    :description-key="'releaseNoteEdit.cancelDescription'" @update:open="cancelDialogOpen = false"
    @confirm="cancelEdit" />
  <div class="flex flex-col w-full items-center px-4 mb-20">
    <div class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10 flex items-center gap-4 max-w-[calc(100%-2rem)] lg:max-w-[calc(100%-5rem)]">
      <Button variant="outline" class="shrink-0" @click="$router.back()">
        <ArrowLeft />{{ t('button.previous') }}
      </Button>
      <Breadcrumb class="text-text-primary min-w-0">
        <BreadcrumbList class="min-w-0">
          <BreadcrumbItem class="shrink-0">
            <BreadcrumbLink href="/">{{ t('title.releaseNotes') }}</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator class="shrink-0" />
          <BreadcrumbItem class="min-w-0">
            <span class="truncate">{{ releaseNote?.tag }}</span>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    </div>

    <form class="w-full flex flex-col items-center" @submit="onSubmit">
      <div 
      ref="releaseNoteRef"
        class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
        <div class="flex flex-col gap-8 w-full">
          <div data-pdf-exclude class="flex sm:hidden gap-4 ml-auto">
            <Button 
            :disabled="disableSummarizeButton" type="button"
              @click="onSummarize" variant="glow">
              {{ t('button.summarize') }}
              <Spinner size="sm" v-if="loadingSummary" class="h-4 dark:text-text-primary"/>
              <Sparkles v-else/>
            </Button>
            <Button type="button" class="" variant="outline" @click="onCancel">{{ t('button.cancel')
              }}
              <Ban />
            </Button>
            <Button variant="outline" type="submit">{{
              t('button.save') }}
              <Save />
            </Button>
          </div>
          <div class="flex flex-row items-center justify-between w-full">
            <div class="flex items-center gap-4">
              <div class="flex flex-col gap-1">
                <h1 class="text-lg">{{ t('title.title') }}</h1>
                <Input class="w-full" v-model="tag" :placeholder="t('placeholder.title')" />
              </div>
            </div>
            <div data-pdf-exclude class="hidden sm:flex gap-4 mt-auto">
              <Tooltip>
                <TooltipTrigger as-child>
                  <div class="inline-flex">
                    <Button 
                    :disabled="disableSummarizeButton" type="button"
                      @click="onSummarize"
                      variant="glow">
                      {{ t('button.summarize') }}
                      <Spinner size="sm" v-if="loadingSummary" class="h-4 dark:text-text-primary"/>
                      <Sparkles v-else/>
                    </Button>
                  </div>
                </TooltipTrigger>
                <TooltipContent v-if="disableSummarizeButton">
                  {{ t('tooltip.noCommits') }}
                </TooltipContent>
                <TooltipContent v-else>
                  {{ t('tooltip.explainSummarize') }}
                </TooltipContent>
              </Tooltip>
              <Button type="button" variant="outline" @click="onCancel">{{ t('button.cancel') }}
                <Ban />
              </Button>
              <Button variant="outline" type="submit">{{
                t('button.save') }}
                <Save />
              </Button>
            </div>
          </div>

          <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.description') }}</h1>
            <Textarea
            class="w-full" v-model="summary"
              :placeholder="t('placeholder.description')" />
          </div>
          <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.product') }}</h1>
            <TagCombobox mode="product" v-model="productId" />
          </div>
          <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.releaseTimeline') }}</h1>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 w-fit">
              <p class="self-center">{{ `${t('title.previewAvailableFrom')}:` }}</p>
              <DatePicker v-model="previewAvailableFrom" :placeholder="t('placeholder.toBeDetermined')"/>
              <p class="self-center">{{ `${t('title.recommendedTestPhaseFrom')}:` }}</p>
              <DatePicker v-model="recommendedTestPhaseFrom" :max="recommendedTestPhaseTo" :placeholder="t('placeholder.toBeDetermined')"/>
              <p class="self-center">{{ `${t('title.recommendedTestPhaseTo')}:` }}</p>
              <DatePicker v-model="recommendedTestPhaseTo" :min="recommendedTestPhaseFrom" :placeholder="t('placeholder.toBeDetermined')"/>
              <p class="self-center">{{ `${t('title.plannedProductionDeployment')}:` }}</p>
              <DatePicker v-model="plannedProductionDeployment" :placeholder="t('placeholder.toBeDetermined')"/>
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.changeImpacts') }}</h1>
            <ChangeImpactTable v-model="changeImpacts" :editable="true"/>
          </div>
          <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.knownLimitations') }}</h1>
            <div class="flex flex-col gap-2">
              <div v-for="(_, index) in knownLimitations" :key="index" class="flex flex-row gap-2">
                <Input class="w-full" v-model="knownLimitations[index]" :placeholder="t('placeholder.knownLimitation')" />
                <Button type="button" variant="outline" size="icon" @click="removeKnownLimitation(index)">
                  <Trash2 />
                </Button>
              </div>
              <Button type="button" variant="outline" class="w-fit" @click="addKnownLimitation">
                {{ t('button.addKnownLimitation') }}
                <Plus />
              </Button>
            </div>
          </div>
        </div>
        <div class="flex flex-col w-full gap-10">
          <div class="flex flex-col gap-4">
            <div class="flex flex-col gap-1">
              <h1 class="text-lg">{{ t('title.changeNotes') }}</h1>
              <MultiselectChangeNotes 
                @update:model-value="onChangeNotesUpdate"
                v-model="changeNoteIdsWithinReleaseNote" />
              <Button
                variant="outline"
                type="button"
                class="w-fit"
                @click="changeNoteIdsWithinReleaseNote = []"
              >
                {{ t('button.clearChangeNotes') }}
              </Button>
            </div>
            <div class="flex flex-col gap-1">
              <h2 class="text-lg">{{ t('title.addFromRepository') }}</h2>
              <p
                v-if="availableGitRepositories == undefined || availableGitRepositories.length < 1">
                {{ t('repositories.noGitRepositories') }}</p>
              <div v-else class="flex flex-col sm:flex-row gap-2 justify-between">
                <SelectGitRepository v-model="gitRepository" />
                <SelectChangeNotes 
                :placeholder="t('title.from')" :disabled="gitRepository === null"
                  :change-notes="currentGitRepositoryChangeNotes ?? []" v-model="fromChangeNote" />
                <SelectChangeNotes 
                :placeholder="t('title.to')" :disabled="gitRepository === null"
                  :change-notes="currentGitRepositoryChangeNotes ?? []" v-model="toChangeNote" />
                <Button 
                class="w-45" :disabled="gitRepository === null || currentGitRepositoryChangeNotes === undefined || currentGitRepositoryChangeNotes.length === 0" type="button"
                  variant="outline"
                  @click.stop="onChangeNoteRangeChange(gitRepository?.id ?? -1)">{{
                    t('button.updateRange') }}</Button>
              </div>
              <p v-if="gitRepository !== null && !currentGitRepositoryChangeNotes?.length && isCurrentGitRepositoryChangeNotesFetched">
                {{ t('repositories.noChangeNotesInRepository') }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>
