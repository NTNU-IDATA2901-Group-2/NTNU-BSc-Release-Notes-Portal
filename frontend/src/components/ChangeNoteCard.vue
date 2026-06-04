<script setup lang="ts">
import type { PrimitiveProps } from 'reka-ui';
import Badge from './ui/badge/Badge.vue';
import type { ChangeNote } from '@/utils/types';
import { Checkbox } from './ui/checkbox';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { Tooltip, TooltipContent, TooltipTrigger } from './ui/tooltip';
import { openJiraTicket } from '@/utils/jira.ts';

const props = defineProps<PrimitiveProps & {
  changeNote: ChangeNote
}>()

const selected = defineModel<boolean>({ required: true })
const { t } = useI18n();

const changeNote = props.changeNote;

const handleCheckboxClick = (event: Event) => {
  event.preventDefault();
  event.stopPropagation();
}

</script>

<template>
  <RouterLink :to="`/change-notes/${changeNote.id}`" class="block">

    <div
      class="flex flex-col p-4 gap-2 min-h-30 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
      <div class="flex justify-between gap-4">
        <div class="flex gap-4 items-center min-w-0">

          <div @click="handleCheckboxClick" class="flex items-center" v-if="isAdmin">
            <Checkbox v-model="selected" :disabled="false" />
          </div>

          <h3 v-if="changeNote.title" class="text-xl whitespace-nowrap overflow-hidden text-ellipsis">{{ changeNote.title }}</h3>
          <h3 v-else class="text-xl text-text-primary/50">{{ t('placeholder.noTitle') }}</h3>
        </div>
        <div class="flex gap-2 items-center">
          <Tooltip v-if="isAdmin && changeNote.reference">
            <TooltipTrigger as-child>
              <Badge 
                class="h-6 hover:cursor-pointer hover:underline"
                variant="outline"
                @click.stop.prevent="() => openJiraTicket(changeNote.reference)"
              >
                {{ changeNote.reference }}
              </Badge>
            </TooltipTrigger>
            <TooltipContent>
                {{ t('tooltip.reference') }}
            </TooltipContent>
          </Tooltip>
          <Tooltip v-if="isAdmin">
            <TooltipTrigger as-child>
              <Badge 
                v-if="isAdmin" class="h-6"
                :variant="changeNote.published ? 'success' : 'destructive'"
              >
                {{ changeNote.published ? t('card.published') : t('card.private') }}
              </Badge>
            </TooltipTrigger>
            <TooltipContent>
                {{ changeNote.published ? t('tooltip.publishedNote') : t('tooltip.privateNote') }}
            </TooltipContent>
          </Tooltip>
        </div>
        
      </div>

      <div class="flex flex-row gap-2 mt-auto">
        <Tooltip v-if="changeNote.product">
          <TooltipTrigger as-child>
            <Badge class="h-6">{{ changeNote.product.name }}</Badge>
          </TooltipTrigger>
          <TooltipContent>
            {{ t('title.product') }}
          </TooltipContent>
        </Tooltip>

        <Tooltip v-if="changeNote.scope">
          <TooltipTrigger as-child>
            <Badge class="h-6">{{ changeNote.scope.name }}</Badge>
          </TooltipTrigger>
          <TooltipContent>
            {{ t('title.scope') }}
          </TooltipContent>
        </Tooltip>

        <Tooltip v-if="changeNote.feature">
          <TooltipTrigger as-child>
            <Badge class="h-6">{{ changeNote.feature.name }}</Badge>
          </TooltipTrigger>
          <TooltipContent>
            {{ t('title.feature') }}
          </TooltipContent>
        </Tooltip>
        <Tooltip v-if="changeNote.customer">
            <TooltipTrigger as-child>
              <Badge v-if="changeNote.customer" :variant="'outline'" class="ml-auto">
                {{ changeNote.customer.name }}
              </Badge>
            </TooltipTrigger>
            <TooltipContent>
              {{ t('title.customer') }}
            </TooltipContent>
          </Tooltip>
      </div>

    </div>
  </RouterLink>
</template>