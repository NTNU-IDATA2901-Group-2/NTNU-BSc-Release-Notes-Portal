<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';
import Separator from '@/components/ui/separator/Separator.vue';
import Badge from '@/components/ui/badge/Badge.vue';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useGetReleaseNote, useArchiveReleaseNote, useUpdateReleaseNote, usePublishReleaseNote } from '@/api/release-note-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';

import { Pencil, Trash2, Eye, EyeOff, FileDown, Ban, Save, ArrowLeft, EllipsisVertical, Sparkles } from "lucide-vue-next"
import { computed, ref } from 'vue';
import Input from '@/components/ui/input/Input.vue';
import { Textarea } from '@/components/ui/textarea';
import DeletePrompt from '@/components/DeletePrompt.vue';
import MultiselectChangeNotes from '@/components/MultiselectChangeNotes.vue';
import { routeNames, router } from '@/utils/router';
import { toast } from 'vue-sonner';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { EditReleaseNoteSchema } from '@/schemas';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../components/ui/breadcrumb';
import { useI18n } from 'vue-i18n';
import md from '@/utils/markdown-it';
import { exportToPdf } from '@/utils/pdf';
import { isAdmin } from '@/utils/keycloak';
import { useTranslate } from '@/api/ai';
import type { ChangeNote } from '@/utils/types';

const isEditing = ref(false)

const route = useRoute();
const { t, locale } = useI18n();

const id = route.params.id as string;

const { isPending, isFetching, isError, data: releaseNote } = useGetReleaseNote(id);

const { mutate: archiveReleaseNote } = useArchiveReleaseNote(id,
  {
    onSettled: () => {
      deletePromptOpen.value = false;
    },
    onSuccess: () => {
      router.push(routeNames.releaseNotes);
      toast.success(t('toast.releaseNoteDeletedSuccess'));
    },
    onError: () => {
      toast.error(t('toast.releaseNoteDeleteError'));
    }
  }
);

const deletePromptOpen = ref(false);

const changeNotes = ref<number[]>(releaseNote.value?.changeNotes?.map(cn => cn.id) || [])

const form = useForm({
  validationSchema: toTypedSchema(EditReleaseNoteSchema),
  initialValues: {
    tag: '',
    summary: '',
    changeNoteIds: [],
    published: false,
  }
})

const [tag] = form.defineField('tag');
const [summary] = form.defineField('summary');

const startEditing = () => {
  if (!releaseNote.value) return

  changeNotes.value = releaseNote.value.changeNotes.map(c => c.id);

  form.setValues({
    tag: releaseNote.value.tag ?? '',
    summary: releaseNote.value.summary ?? '',
    changeNoteIds: releaseNote.value.changeNotes.map(c => c.id),
    published: releaseNote.value.published,
  })

  isEditing.value = true
}

const onSubmit = form.handleSubmit((values) => {
  if (releaseNote.value !== undefined) {
    const payload = {
      ...values,
      changeNoteIds: changeNotes.value,
    }
    updateReleaseNoteMutation.mutate({ id: releaseNote.value.id, dto: payload });
    isEditing.value = false;
  }
})

const updateReleaseNoteMutation = useUpdateReleaseNote({
  onSuccess: () => {
    toast.success(t('toast.releaseNoteUpdatedSuccess'));
  },
  onError: () => {
    toast.error(t('toast.releaseNoteUpdateError'));
  }
})

const handlePublish = () => {
  if (releaseNote.value !== undefined) {
    publishReleaseNoteMutation.mutate({ id: releaseNote.value.id, publish: !releaseNote.value?.published });
  }
}

const publishReleaseNoteMutation = usePublishReleaseNote({
  onSuccess: () => {
    toast.success(releaseNote.value?.published ? t('toast.releaseNoteUnpublished') : t('toast.releaseNotePublished'));
  },
  onError: () => {
    toast.error(t('toast.releaseNotePublishError'));
  }
})

const releaseNoteRef = ref<HTMLDivElement>();

const handleExport = () => {
  if (!releaseNote.value) return;
  try {
    exportToPdf(releaseNote.value.tag, releaseNoteRef);
  } catch (error) {
    console.error('Error exporting to PDF:', error);
    toast.error(t('toast.exportPdfError'));
  }
}

const translateMutation = useTranslate({
  onSuccess: () => {
    toast.success(t('toast.translationSuccess'));
  },
  onError: () => {
    toast.error(t('toast.translationError'));
  },
});

const translatedChangeNotes = ref<ChangeNote[] | null>(null);
const translatedSummary = ref<string | null>(null);
const hasTranslation = computed(() => translatedSummary.value !== null || translatedChangeNotes.value !== null);

const onTranslate = async () => {
  if (translatedSummary.value || translatedChangeNotes.value) {
    translatedSummary.value = null;
    translatedChangeNotes.value = null;
    return;
  }

  const summaryResult = await translateMutation.mutateAsync({ 
    text: releaseNote.value?.summary || '',
    locale: locale.value,
  });

  translatedSummary.value = summaryResult;

  if (releaseNote.value?.changeNotes) {
    releaseNote.value.changeNotes.forEach(async (changeNote, index) => {
      const result = await translateMutation.mutateAsync({
        text: changeNote.description,
        locale: locale.value,
      });

      if (result) {
        if (!translatedChangeNotes.value) translatedChangeNotes.value = [];
        translatedChangeNotes.value[index] = {
          ...changeNote,
          description: result,
        }
      }
    })
  }
}

</script>

<template>
  <div class="flex flex-col w-full items-center px-4 mb-20">
    <DeletePrompt v-model:open="deletePromptOpen" :on-confirm="() => archiveReleaseNote()" />
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
        <Button v-if="isEditing" variant="outline" @click="isEditing = false">{{ t('button.cancel')
          }}
          <Ban />
        </Button>
        <Button v-if="isEditing" variant="outline" type="submit">{{ t('button.save') }}
          <Save />
        </Button>
      </div>
      <Spinner v-if="isPending || isFetching" />
      <h1 v-if="isError">{{ t('loadingError.releaseNotes') }}</h1>

      <div 
        ref="releaseNoteRef"
        v-if="!isPending && !isFetching && !isError && releaseNote"
        class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
        <div class="flex flex-col gap-4 w-full">
          <div class="flex flex-row items-center justify-between w-full">
            <div class="flex items-center gap-4">
              <h1 v-if="!isEditing" class="text-4xl max-w-60 whitespace-nowrap overflow-hidden">{{
                releaseNote.tag }}</h1>
              <div v-if="isEditing" class="flex flex-col gap-1">
                <h4 class="text-md">{{ t('title.title') }}</h4>
                <Input class="w-full" v-model="tag" :placeholder="t('placeholder.title')" />
              </div>
              <Badge 
                data-pdf-exclude
                v-if="!isEditing && isAdmin" class="h-6"
                :variant="releaseNote.published ? 'success' : 'destructive'">{{
                  releaseNote.published ? 'Published' : 'Private' }}</Badge>
            </div>
            <div data-pdf-exclude class="flex gap-4">
              <Button type="button" v-if="!(locale === 'en')" variant="glow" @click="onTranslate">{{hasTranslation ? t('button.undo') : t('button.translate') }} <Sparkles /></Button>
              <DropdownMenu v-if="!isEditing">
                <DropdownMenuTrigger
                  class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                  <EllipsisVertical class="text-text-primary" />
                </DropdownMenuTrigger>
                <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                  <DropdownMenuItem @click="startEditing" v-if="isAdmin">
                    <div class="w-full flex gap-2">
                      <p class="text-text-primary ml-auto">{{ t('button.edit') }}</p>
                      <Pencil class="text-text-primary" />
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem @click="deletePromptOpen = true" v-if="isAdmin">
                    <div class="w-full flex gap-2">
                      <p class="ml-auto text-text-primary">{{ t('button.delete') }}</p>
                      <Trash2 class="text-text-primary" />
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem @click="handlePublish" v-if="isAdmin">
                    <div class="w-full flex gap-2">
                      <p class="ml-auto text-text-primary">{{ !releaseNote.published ?
                        t('button.publish')
                        : t('button.unpublish') }}</p>
                      <component 
                        :is="!releaseNote.published ? Eye : EyeOff"
                        class="text-text-primary" />
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem @click="handleExport">
                    <div class="w-full flex gap-2">
                      <p class="ml-auto text-text-primary">{{ t('button.export') }}</p>
                      <FileDown class="text-text-primary" />
                    </div>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
              <Button 
                class="hidden md:flex" v-if="isEditing" variant="outline"
                @click="isEditing = false">{{ t('button.cancel') }}
                <Ban />
              </Button>
              <Button class="hidden md:flex" v-if="isEditing" variant="outline" type="submit">{{
                t('button.save') }}
                <Save />
              </Button>
            </div>
          </div>

          <p v-if="!isEditing" v-html="md.render(translatedSummary ?? releaseNote.summary)"></p>
          <div class="flex flex-col gap-1" v-if="isEditing">
            <h4 class="text-md">{{ t('title.description') }}</h4>
            <Textarea 
              class="w-full" v-model="summary"
              :placeholder="t('placeholder.description')" />
          </div>
        </div>
        <Separator v-if="!isEditing" class="w-full h-2" />
        <div class="flex flex-col w-full gap-10">
          <div v-if="isEditing" class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.changeNotes') }}</h4>
            <MultiselectChangeNotes v-model="changeNotes" />
          </div>
          <h2 v-else class="text-3xl">{{ t('title.changeNotes') }}</h2>

          <div v-if="!isEditing" class="flex flex-col gap-16">
            <div 
              v-for="change in translatedChangeNotes ?? releaseNote.changeNotes" :key="change.id"
              class="flex flex-col gap-2">

              <h3 class="text-2xl">{{ change.reference }}</h3>
              <div>
                <h3 class="text-xl" data-pdf-exclude>{{ t('title.description') }}</h3>
                <p class="ml-4" v-html="md.render(change.description)"></p>
              </div>
              <div v-if="change.developerNotes" data-pdf-exclude>
                <h3 class="text-xl">{{ t('title.developerNotes') }}</h3>
                <p class="ml-4" v-html="md.render(change.developerNotes)"></p>
              </div>
              <div v-if="change.upgradeNotes" data-pdf-exclude>
                <h3 class="text-xl">{{ t('title.upgradeRequirements') }}</h3>
                <p class="ml-4" v-html="md.render(change.upgradeNotes)"></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>