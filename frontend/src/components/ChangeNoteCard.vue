<script setup lang="ts">
import type { PrimitiveProps } from 'reka-ui';
import Badge from './ui/badge/Badge.vue';
import type { ChangeNote } from '@/utils/types';
import { Checkbox } from './ui/checkbox';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';

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
      class="flex flex-col p-4 gap-2 h-30 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
      <div class="flex justify-between">
        <div class="flex gap-4 items-center">

          <div @click="handleCheckboxClick" class="flex items-center" v-if="isAdmin">
            <Checkbox v-model="selected" :disabled="false" />
          </div>

          <h3 class="text-xl">{{ changeNote.reference }}</h3>

          <Badge v-if="changeNote.customer" :variant="'outline'">{{ changeNote.customer.name }}</Badge>

        </div>
        <div>
          <Badge v-if="isAdmin" :variant="changeNote.published ? 'success' : 'destructive'">{{ changeNote.published
            ? t('card.published') : t('card.private') }}</Badge>
        </div>
      </div>


      <div class="flex flex-row gap-2 mt-auto">
        <Badge v-if="changeNote.scope" :variant="'default'">{{ changeNote.scope.name }}</Badge>
        <Badge v-if="changeNote.feature" :variant="'default'">{{ changeNote.feature.name }}</Badge>
        <Badge v-if="changeNote.product" :variant="'default'">{{ changeNote.product.name }}</Badge>
      </div>

    </div>
  </RouterLink>
</template>