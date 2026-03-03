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
import { useGetReleaseNote, useArchiveReleaseNote, updateReleaseNote, publishReleaseNote } from '@/api/release-note-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';

import { Pencil, Trash2, Eye, EyeOff, FileDown, Ban, Save, ArrowLeft, EllipsisVertical } from "lucide-vue-next"
import { ref } from 'vue';
import Input from '@/components/ui/input/Input.vue';
import { Textarea } from '@/components/ui/textarea';
import DeletePrompt from '@/components/DeletePrompt.vue';
import MultiselectChangeNotes from '@/components/MultiselectChangeNotes.vue';
import { routeNames, router } from '@/utils/router';
import { toast } from 'vue-sonner';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import type { ChangeNote, PersistReleaseNoteDTO } from '@/utils/types';
import { EditReleaseNoteSchema } from '@/schemas';
import { useMutation, useQueryClient } from '@tanstack/vue-query';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../components/ui/breadcrumb';
import { useI18n } from 'vue-i18n';

const isEditing = ref(false)

const route = useRoute();

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

const { t } = useI18n();

const changeNotes = ref<ChangeNote[]>(releaseNote?.value?.changeNotes || [])


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

  changeNotes.value = [...releaseNote.value.changeNotes]

  form.setValues({
    tag: releaseNote.value.tag ?? '',
    summary: releaseNote.value.summary ?? '',
    changeNoteIds: releaseNote.value.changeNotes.map(c => c.id),
    published: releaseNote.value.published,
  })

  isEditing.value = true
}

const onSubmit = form.handleSubmit((values) => {
  const payload = {
    ...values,
    changeNoteIds: changeNotes.value.map(c => c.id),
  }
  updateReleaseNoteMutation.mutate(payload);
  isEditing.value = false;
})

const queryClient = useQueryClient();
const updateReleaseNoteMutation = useMutation({
  mutationFn: (values: PersistReleaseNoteDTO) => updateReleaseNote(id, values),
  onSuccess: () => {
    toast.success(t('toast.releaseNoteUpdatedSuccess'));
    queryClient.invalidateQueries({ queryKey: ['releaseNote', id] });
  }
})

const handlePublish = () => {
  publishReleaseNoteMutation.mutate();
}

const publishReleaseNoteMutation = useMutation({
  mutationFn: () => publishReleaseNote(id, !releaseNote.value?.published),
  onSuccess: () => {
    toast.success(!releaseNote.value?.published ? t('toast.releaseNotePublishedSuccess') : t('toast.releaseNoteUnpublishedSuccess'));
    queryClient.invalidateQueries({ queryKey: ['releaseNote', id] });
  },
  onError: () => {
    toast.error(t('toast.releaseNotePublishError'));
  }
})
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
        <Button v-if="isEditing" variant="outline" @click="isEditing = false">{{ t('button.cancel') }}
          <Ban />
        </Button>
        <Button v-if="isEditing" variant="outline" type="submit">{{ t('button.save') }}
          <Save />
        </Button>
      </div>
      <Spinner v-if="isPending || isFetching" />
      <h1 v-if="isError">{{ t('loadingError.releaseNotes') }}</h1>

      <div 
        v-if="!isPending && !isFetching && !isError && releaseNote"
        class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
        <div class="flex flex-col gap-4 w-full">
          <div class="flex flex-row items-center justify-between w-full">
            <div class="flex items-center gap-4">
              <h1 v-if="!isEditing" class="text-2xl max-w-60 whitespace-nowrap overflow-hidden">{{
                releaseNote.tag }}</h1>
              <div v-if="isEditing" class="flex flex-col gap-1">
                <h4 class="text-md">{{ t('title.title') }}</h4>
                <Input class="w-full" v-model="tag" :placeholder="t('placeholder.title')" />
              </div>
              <Badge 
                v-if="!isEditing" class="h-6"
                :variant="releaseNote.published ? 'success' : 'destructive'">{{
                  releaseNote.published ? 'Published' : 'Private' }}</Badge>
            </div>
            <div class="flex gap-4">
              <DropdownMenu v-if="!isEditing">
                <DropdownMenuTrigger
                  class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                  <EllipsisVertical class="text-text-primary" />
                </DropdownMenuTrigger>
                <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                  <DropdownMenuItem @click="startEditing">
                    <div class="w-full flex gap-2">
                      <p class="text-text-dark-static ml-auto">{{ t('button.edit') }}</p>
                      <Pencil class="text-text-dark-static" />
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem @click="deletePromptOpen = true">
                    <div class="w-full flex gap-2">
                      <p class="ml-auto text-text-dark-static">{{ t('button.delete') }}</p>
                      <Trash2 class="text-text-dark-static" />
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem @click="handlePublish">
                    <div class="w-full flex gap-2">
                      <p class="ml-auto text-text-dark-static">{{ !releaseNote.published ? t('button.publish')
                        : t('button.unpublish') }}</p>
                      <component 
                        :is="!releaseNote.published ? Eye : EyeOff"
                        class="text-text-dark-static" />
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                      <p class="ml-auto text-text-dark-static">{{ t('button.export') }}</p>
                      <FileDown class="text-text-dark-static" />
                    </div>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
              <Button 
                class="hidden md:flex" v-if="isEditing" variant="outline"
                @click="isEditing = false">{{ t('button.cancel') }}
                <Ban />
              </Button>
              <Button class="hidden md:flex" v-if="isEditing" variant="outline" type="submit">{{ t('button.save') }}
                <Save />
              </Button>
            </div>
          </div>

          <p v-if="!isEditing" class="">{{ releaseNote.summary }}</p>
          <div class="flex flex-col gap-1" v-if="isEditing">
            <h4 class="text-md">{{ t('title.description') }}</h4>
            <Textarea class="w-full" v-model="summary" :placeholder="t('placeholder.description')" />
          </div>
        </div>
        <Separator class="w-full h-2" />
        <div class="flex flex-col w-full gap-10">
          <div class="flex flex-col w-full gap-10">
            <h2 class="text-xl">Change Notes</h2>
            <MultiselectChangeNotes v-if="isEditing" v-model="changeNotes" />
            <div v-if="!isEditing">
              <div 
                v-for="change in releaseNote.changeNotes" :key="change.id"
                class="flex flex-col gap-4">
                
                <h3 class="text-lg">{{ change.reference }}</h3>
                <div>
                  <h3 class="text-lg">{{ t('title.description') }}</h3>
                  <p class="text-sm">{{ change.description }}</p>
                </div>
                <div>
                  <h3 class="text-lg">{{ t('title.developerNotes') }}</h3>
                  <p class="text-sm">{{ change.developerNotes }}</p>
                </div>
                <div>
                  <h3 class="text-lg">{{ t('title.upgradeRequirements') }}</h3>
                  <p class="text-sm">{{ change.upgradeNotes }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>