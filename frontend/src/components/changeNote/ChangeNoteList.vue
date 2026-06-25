<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { isAdmin } from '@/utils/keycloak';
import { routeNames } from '@/utils/router';
import md from '@/utils/markdown-it';
import { openJiraTicket } from '@/utils/jira.ts';
import { useCopyToClipboard } from '@/composables/useCopyToClipboard';
import type { ChangeNote, Customer } from '@/utils/types';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import Checkbox from '../ui/checkbox/Checkbox.vue';
import Select from '../ui/select/Select.vue';
import SelectTrigger from '../ui/select/SelectTrigger.vue';
import SelectValue from '../ui/select/SelectValue.vue';
import SelectContent from '../ui/select/SelectContent.vue';
import SelectGroup from '../ui/select/SelectGroup.vue';
import SelectItem from '../ui/select/SelectItem.vue';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { Check, Copy } from 'lucide-vue-next';

const props = defineProps<{
  changeNotes: ChangeNote[];
  translatedChangeNotes?: ChangeNote[] | null;
  hasTranslation?: boolean;
}>();

const { t } = useI18n();
const { copiedKey, copy } = useCopyToClipboard();

const generalChangesChecked = ref(true);
const draftChangesChecked = ref(true);
const customerFilter = ref<number>(-1);

const uniqueCustomers = computed(() => {
  const customerArray = new Array<Customer>();
  props.changeNotes.forEach(change => {
    if (change.customer) {
      if (!customerArray.some(c => c.id === change.customer?.id)) {
        customerArray.push(change.customer);
      }
    }
  });
  return Array.from(customerArray);
});

const shouldShowChangeNote = (change: ChangeNote) => {
  if (!change.published && !draftChangesChecked.value) {
    return false;
  }
  if (change.customer === null) {
    return generalChangesChecked.value;
  }
  if (customerFilter.value === -1) {
    return true;
  }
  return change.customer.id === customerFilter.value;
};

const filteredChangeNotes = computed(() =>
  (props.translatedChangeNotes ?? props.changeNotes).filter(shouldShowChangeNote));

defineExpose({ filteredChangeNotes });
</script>

<template>
  <div class="flex flex-col w-full gap-10">
    <div class="flex gap-4 flex-col md:flex-row justify-between items-start">
      <h2 class="text-3xl">{{ t('title.changeNotes') }}</h2>
      <div class="flex flex-col sm:flex-row items-center gap-4">
        <div class="flex gap-2">
          <p>{{ t('button.showGeneralChanges') }}</p>
          <Checkbox v-model="generalChangesChecked" class="cursor-pointer" />
        </div>
        <div class="flex gap-2">
          <p>{{ t('button.showDraftChanges') }}</p>
          <Checkbox v-model="draftChangesChecked" class="cursor-pointer" />
        </div>
        <div>
          <Select v-model="customerFilter">
            <SelectTrigger class="w-42">
              <SelectValue :placeholder="t('placeholder.filterByCustomer')" />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                <SelectItem :value=-1 class="text-text-primary/50">
                  {{ t('button.allCustomers') }}
                </SelectItem>
              </SelectGroup>
              <SelectGroup>
                <SelectItem v-for="customer in uniqueCustomers" :key="customer.id" :value="customer.id">
                  {{ customer.name }}
                </SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

      </div>
    </div>
    <div class="flex flex-col gap-16">
      <div class="flex flex-col gap-10">
        <p class="text-text-primary/50" v-if="changeNotes.length === 0">{{
          t('placeholder.noChangeNotesAdded')
          }}</p>
        <div v-for="change in filteredChangeNotes" :key="change.id" class="flex flex-col gap-2">
          <div class="flex items-center gap-4">
            <RouterLink
class="text-2xl dark:text-text-dark-static text-text-light-static hover:underline"
              :to="`${routeNames.changeNotes}/${change.id}`">{{ change.title || t('placeholder.noTitle') }}
            </RouterLink>

            <Tooltip v-if="change.customer">
              <TooltipTrigger as-child>
                <Badge v-if="change.customer" :variant="'outline'">{{ change.customer.name }}</Badge>
              </TooltipTrigger>
              <TooltipContent>
                {{ t('title.customer') }}
              </TooltipContent>
            </Tooltip>

            <Tooltip v-if="isAdmin && change.reference">
              <TooltipTrigger as-child>
                <Badge
class="h-6 hover:cursor-pointer hover:underline" variant="outline"
                  @click="() => openJiraTicket(change.reference)">
                  {{ change.reference }}
                </Badge>
              </TooltipTrigger>
              <TooltipContent>
                {{ t('tooltip.reference') }}
              </TooltipContent>
            </Tooltip>

          </div>
          <p v-if="change.viewableByEveryone" class="text-text-primary/50">{{
            t('changeNote.changeNoteViewableByEveryone') }}</p>
          <div>
            <div class="flex justify-between align-center">
              <div>
                <h3 class="text-xl">{{ t('title.description') }}</h3>
                <p class="ml-4" v-if="change.description" v-html="md.render(change.description)"></p>
              </div>
              <Button
variant="outline" size="icon-sm"
                @click="copy(hasTranslation ? translatedChangeNotes?.find(c => c.id === change.id)?.description ?? '' : change.description ?? '', `change-${change.id}`)">
                <component :is="copiedKey === `change-${change.id}` ? Check : Copy" />
              </Button>
            </div>
            <p v-if="hasTranslation" class="text-text-primary/50 text-right">{{
              t('ai.translationDisclaimer') }}</p>
          </div>
          <div v-if="change.developerNotes">
            <h3 class="text-xl">{{ t('title.developerNotes') }}</h3>
            <p class="ml-4" v-html="md.render(change.developerNotes)"></p>
          </div>
          <div v-if="change.upgradeNotes">
            <h3 class="text-xl">{{ t('title.upgradeRequirements') }}</h3>
            <p class="ml-4" v-html="md.render(change.upgradeNotes)"></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
