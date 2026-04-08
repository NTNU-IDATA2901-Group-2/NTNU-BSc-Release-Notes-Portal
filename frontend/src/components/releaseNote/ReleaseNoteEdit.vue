<script setup lang="ts">
import { useUpdateReleaseNote } from '@/api/release-note-api';
import { EditReleaseNoteSchema } from '@/schemas';
import type { GitRepository, ChangeNote, ReleaseNote } from '@/utils/types';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue-sonner';
import { Button } from '../ui/button';
import { ArrowLeft, Ban, Save, Sparkles } from 'lucide-vue-next';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import MultiselectChangeNotes from '../MultiselectChangeNotes.vue';
import SelectChangeNotes from '../SelectChangeNotes.vue';
import { useGetChangeNotes } from '@/api/change-note-api';
import SelectGitRepository from '../SelectGitRepository.vue';
import { useSummarizeReleaseNote } from '@/api/ai-api';

const { t } = useI18n();

const props = defineProps<{
  releaseNote: ReleaseNote;
}>();

const isEditing = defineModel("isEditing", { type: Boolean, required: true });

const releaseNote = props.releaseNote;

const changeNoteIdsWithinReleaseNote = ref<number[]>(releaseNote.changeNotes?.map(cn => cn.id) || [])

const { data: availableChangeNotes } = useGetChangeNotes()

const gitRepository = ref<GitRepository | null>(null)
const params = computed(() => {
  return { gitRepositoryIds: gitRepository.value?.id ? gitRepository.value?.id.toString() : '' }
});
const { data: currentGitRepositoryChangeNotes } = useGetChangeNotes(params)

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

const summarizeReleaseNote = useSummarizeReleaseNote({
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
  }
})

const form = useForm({
  validationSchema: toTypedSchema(EditReleaseNoteSchema),
  initialValues: {
    tag: releaseNote.tag || '',
    summary: releaseNote.summary || '',
    changeNoteIds: changeNoteIdsWithinReleaseNote.value,
    published: releaseNote.published,
  }
})

const onCancel = () => {
  isEditing.value = false;
}

const onSubmit = form.handleSubmit((values) => {
  if (releaseNote !== undefined) {
    const payload = {
      ...values,
      changeNoteIds: changeNoteIdsWithinReleaseNote.value,
    }
    updateReleaseNoteMutation.mutate({ id: releaseNote.id, dto: payload });
  }
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

</script>


<template>
   <div class="flex flex-col w-full items-center px-4 mb-20">
    <div class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10 flex items-center gap-4">
      <Button variant="outline" class="" @click="$router.back()">
        <ArrowLeft />{{ t('button.previous') }}
      </Button>
      <Breadcrumb class="text-text-primary">
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/">{{ t('title.releaseNotes') }}</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            {{ releaseNote?.tag }}
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    </div>

    <form class="w-full flex flex-col items-center" @submit="onSubmit">
      <div class="md:hidden flex w-full mt-4 justify-end gap-2">
        <Button variant="outline" @click="onCancel">
          {{ t('button.cancel') }}
          <Ban />
        </Button>
        <Button variant="outline" type="submit">{{ t('button.save') }}
          <Save />
        </Button>
      </div>

      <div 
        ref="releaseNoteRef"
        class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
        <div class="flex flex-col gap-4 w-full">
          <div class="flex flex-row items-center justify-between w-full">
            <div class="flex items-center gap-4">
              <div class="flex flex-col gap-1">
                <h1 class="text-2xl">{{ t('title.title') }}</h1>
                <Input class="w-full" v-model="tag" :placeholder="t('placeholder.title')" />
              </div>
            </div>
            <div data-pdf-exclude class="flex gap-4 mt-auto">
              <Button
                type="button"
                @click="summarizeReleaseNote.mutate(releaseNote.id)"
                variant="glow"
              >
                {{t('button.summarize')}}
                <Sparkles />
              </Button>
              <Button 
                class="hidden md:flex" variant="outline"
                @click="onCancel">{{ t('button.cancel') }}
                <Ban />
              </Button>
              <Button class="hidden md:flex" variant="outline" type="submit">{{
                t('button.save') }}
                <Save />
              </Button>
            </div>
          </div>

          <div class="flex flex-col gap-1">
            <h1 class="text-2xl">{{ t('title.description') }}</h1>
            <Textarea 
              class="w-full" v-model="summary"
              :placeholder="t('placeholder.description')" />
          </div>
        </div>
        <div class="flex flex-col w-full gap-10">
          <div class="flex flex-col gap-1">
            <h1 class="text-2xl">{{ t('title.changeNotes') }}</h1>
            <MultiselectChangeNotes @update:model-value="onChangeNotesUpdate" v-model="changeNoteIdsWithinReleaseNote" />
            <h2 class="text-xl">{{ t('title.addFromRepository') }}</h2>
            <div class="flex flex-row justify-between">
              <SelectGitRepository v-model="gitRepository" />
              <div class="flex flex-row gap-2">
                <h2 class="text-lg">{{ t('title.from') }}</h2>
                <SelectChangeNotes :change-notes="currentGitRepositoryChangeNotes ?? []" v-model="fromChangeNote" />
              </div>
              <div class="flex flex-row gap-2">
                <h2 class="text-lg">{{ t('title.to') }}</h2>
                <SelectChangeNotes :change-notes="currentGitRepositoryChangeNotes ?? []" v-model="toChangeNote" />
              </div>
              <Button type="button" variant="outline" @click.stop="onChangeNoteRangeChange(gitRepository?.id ?? -1)">{{ t('button.updateRange') }}</Button>
              
            </div>
            
          </div>
        </div>
      </div>
    </form>
  </div>
</template>