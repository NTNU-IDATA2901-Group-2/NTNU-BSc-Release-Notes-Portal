<script setup lang="ts">
import { ref } from 'vue';
import { exportToPdf } from '@/utils/pdf';
import { useArchiveReleaseNote, usePublishReleaseNote } from '@/api/release-note-api';
import type { ReleaseNote } from '@/utils/types';
import { useRouter } from 'vue-router';
import { routeNames } from '@/utils/router';
import { toast } from 'vue-sonner';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { Pencil, Trash2, Eye, EyeOff, FileDown, ArrowLeft, EllipsisVertical } from "lucide-vue-next"
import md from '@/utils/markdown-it';
import DeletePrompt from '../DeletePrompt.vue';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbSeparator } from '../ui/breadcrumb';
import { Badge } from '../ui/badge';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '../ui/dropdown-menu';
import { Separator } from '../ui/separator';
import { Button } from '../ui/button';

const props = defineProps<{
  releaseNote: ReleaseNote;
}>();

const isEditing = defineModel("isEditing", { type: Boolean, required: true });

const router = useRouter();
const { t } = useI18n();

const releaseNote = props.releaseNote;

const deletePromptOpen = ref(false);

const { mutate: archiveReleaseNote } = useArchiveReleaseNote(releaseNote.id.toString(),
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

const startEditing = () => {
  isEditing.value = true;
}


const handlePublish = () => {
  if (releaseNote !== undefined) {
    publishReleaseNoteMutation.mutate({ id: releaseNote.id, publish: !releaseNote.published });
  }
}

const publishReleaseNoteMutation = usePublishReleaseNote({
  onSuccess: () => {
    toast.success(releaseNote.published ? t('toast.releaseNoteUnpublished') : t('toast.releaseNotePublished'));
  },
  onError: () => {
    toast.error(t('toast.releaseNotePublishError'));
  }
})


const releaseNoteRef = ref<HTMLDivElement>();

const handleExport = () => {
  if (!releaseNote) return;
  try {
    exportToPdf(releaseNote.tag, releaseNoteRef);
  } catch (error) {
    console.error('Error exporting to PDF:', error);
    toast.error(t('toast.exportPdfError'));
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

    <div 
      ref="releaseNoteRef"
      class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <h1 class="text-4xl max-w-60 whitespace-nowrap overflow-hidden">{{
              releaseNote.tag }}</h1>
            <Badge 
              data-pdf-exclude v-if="isAdmin" class="h-6"
              :variant="releaseNote.published ? 'success' : 'destructive'">{{
                releaseNote.published ? 'Published' : 'Private' }}</Badge>
          </div>
          <div data-pdf-exclude class="flex gap-4">
            <DropdownMenu>
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
          </div>
        </div>

        <p v-if="releaseNote.summary" v-html="md.render(releaseNote.summary)"></p>
      </div>
      <Separator class="w-full h-2" />
      <div class="flex flex-col w-full gap-10">
        <h2 class="text-3xl">{{ t('title.changeNotes') }}</h2>

        <div class="flex flex-col gap-16">
          <div 
            v-for="change in releaseNote.changeNotes" :key="change.id"
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
  </div>
</template>