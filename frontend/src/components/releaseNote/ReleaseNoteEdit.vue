<script setup lang="ts">
import { useUpdateReleaseNote } from '@/api/release-note-api';
import { EditReleaseNoteSchema } from '@/schemas';
import { type ChangeNote, type ReleaseNote } from '@/utils/types';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue-sonner';
import { Button } from '../ui/button';
import { ArrowLeft, Ban, Save } from 'lucide-vue-next';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import MultiselectChangeNotes from '../MultiselectChangeNotes.vue';
import SelectChangeNotes from '../SelectChangeNotes.vue';
import { useGetChangeNotes } from '@/api/change-note-api';
import DialogPrompt from '../DialogPrompt.vue';
import { onBeforeRouteLeave } from 'vue-router';

const { t } = useI18n();

const props = defineProps<{
  releaseNote: ReleaseNote;
}>();

const isEditing = defineModel("isEditing", { type: Boolean, required: true });
const cancelDialogOpen = ref(false);

const releaseNote = props.releaseNote;

const changeNoteIdsWithinReleaseNote = ref<number[]>(releaseNote.changeNotes?.map(cn => cn.id) || [])

const { data: availableChangeNotes } = useGetChangeNotes()

const fromChangeNote = ref<ChangeNote | null>(null);
const toChangeNote = ref<ChangeNote | null>(null);

const onFromChangeNoteUpdate = (value: ChangeNote | null) => {
  fromChangeNote.value = value;
  onChangeNoteRangeChange();
}

const onToChangeNoteUpdate = (value: ChangeNote | null) => {
  toChangeNote.value = value;
  onChangeNoteRangeChange();
}

const onChangeNotesUpdate = (value: number[]) => {
  changeNoteIdsWithinReleaseNote.value = value;
  fromChangeNote.value = null;
  toChangeNote.value = null;
}

const onChangeNoteRangeChange = () => {
  const fromChangeNoteValue = fromChangeNote.value
  const toChangeNoteValue = toChangeNote.value
  const fromIndex = availableChangeNotes.value?.findIndex((cn) => cn.id === fromChangeNoteValue?.id) ?? -1;
  const toIndex = availableChangeNotes.value?.findIndex((cn) => cn.id === toChangeNoteValue?.id) ?? -1;

  if (fromChangeNoteValue !== null && toChangeNoteValue !== null && fromIndex !== -1 && toIndex !== -1) {
    changeNoteIdsWithinReleaseNote.value = availableChangeNotes.value?.map(cn => cn.id).slice(fromIndex,toIndex + 1) ?? []
  } else if (fromChangeNoteValue !== null && toChangeNoteValue === null && fromIndex !== -1) {
    changeNoteIdsWithinReleaseNote.value = availableChangeNotes.value?.map(cn => cn.id).slice(fromIndex) ?? []  
  } else if (fromChangeNoteValue === null && toChangeNoteValue !== null && toIndex !== -1) {
    changeNoteIdsWithinReleaseNote.value = availableChangeNotes.value?.map(cn => cn.id).slice(0,toIndex + 1) ?? []
  } else {
    changeNoteIdsWithinReleaseNote.value = []
  }
}

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
  cancelDialogOpen.value = true;
}

const cancelEdit = () => {
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

// Warn user of unsaved changes when trying to leave the page
const beforeUnloadListener = (event: BeforeUnloadEvent) => {
     event.preventDefault();
    ;(event as unknown as { returnValue: string }).returnValue = ''
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
</script>


<template>
  <DialogPrompt :open="cancelDialogOpen" :mode="'confirm'" :title-key="'releaseNoteEdit.cancelTitle'" :description-key="'releaseNoteEdit.cancelDescription'" @update:open="cancelDialogOpen = false" @confirm="cancelEdit" />
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
        <Button type="button" variant="outline" @click="onCancel">
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
                <h1 class="text-lg">{{ t('title.title') }}</h1>
                <Input class="w-full" v-model="tag" :placeholder="t('placeholder.title')" />
              </div>
            </div>
            <div data-pdf-exclude class="flex gap-4">
              <Button 
                type="button"
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
            <h1 class="text-lg">{{ t('title.description') }}</h1>
            <Textarea 
              class="w-full" v-model="summary"
              :placeholder="t('placeholder.description')" />
          </div>
        </div>
        <div class="flex flex-col w-full gap-10">
          <div class="flex flex-col gap-1">
            <h1 class="text-lg">{{ t('title.changeNotes') }}</h1>
            <MultiselectChangeNotes @update:model-value="onChangeNotesUpdate" v-model="changeNoteIdsWithinReleaseNote" />
            <h2>{{ t('title.from') }}</h2>
            <SelectChangeNotes @update:model-value="onFromChangeNoteUpdate" v-model="fromChangeNote" />
            <h2>{{ t('title.to') }}</h2>
            <SelectChangeNotes @update:model-value="onToChangeNoteUpdate" v-model="toChangeNote" />
          </div>
        </div>
      </div>
    </form>
  </div>
</template>