<script setup lang="ts">
import { useUpdateReleaseNote } from '@/api/release-note-api';
import { EditReleaseNoteSchema } from '@/schemas';
import type { ReleaseNote } from '@/utils/types';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vue-sonner';
import { Button } from '../ui/button';
import { ArrowLeft, Ban, Save } from 'lucide-vue-next';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import MultiselectChangeNotes from '../MultiselectChangeNotes.vue';

const { t } = useI18n();

const props = defineProps<{
  releaseNote: ReleaseNote;
}>();

const isEditing = defineModel("isEditing", { type: Boolean, required: true });

const releaseNote = props.releaseNote;

const changeNotes = ref<number[]>(releaseNote.changeNotes?.map(cn => cn.id) || [])

const form = useForm({
  validationSchema: toTypedSchema(EditReleaseNoteSchema),
  initialValues: {
    tag: releaseNote.tag,
    summary: releaseNote.summary,
    changeNoteIds: changeNotes.value,
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
      changeNoteIds: changeNotes.value,
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
            <BreadcrumbLink href="/">Release Notes</BreadcrumbLink>
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
                <h4 class="text-md">{{ t('title.title') }}</h4>
                <Input class="w-full" v-model="tag" :placeholder="t('placeholder.title')" />
              </div>
            </div>
            <div data-pdf-exclude class="flex gap-4">
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
            <h4 class="text-md">{{ t('title.description') }}</h4>
            <Textarea 
              class="w-full" v-model="summary"
              :placeholder="t('placeholder.description')" />
          </div>
        </div>
        <div class="flex flex-col w-full gap-10">
          <div class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.changeNotes') }}</h4>
            <MultiselectChangeNotes v-model="changeNotes" />
          </div>
        </div>
      </div>
    </form>
  </div>
</template>