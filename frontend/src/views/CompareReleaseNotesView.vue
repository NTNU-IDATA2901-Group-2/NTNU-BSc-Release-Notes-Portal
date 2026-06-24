<script setup lang="ts">
import TagSelect from '@/components/TagSelect.vue';
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const selectedProductId = ref<number | undefined>(undefined);
const selectedFromReleaseNoteId = ref<number | undefined>(undefined);
const selectedToReleaseNoteId = ref<number | undefined>(undefined);

const searchParams = computed(() => selectedProductId.value === -1 || selectedProductId.value === undefined ? undefined : {
    productIds: selectedProductId.value.toString()
  } 
)
</script>

<template>
  <div class="flex flex-col items-center mt-20 w-full">
    <div class="flex flex-row gap-8">
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.product') }}</h2>
        <TagSelect mode="product" v-model="selectedProductId" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.fromReleaseNote') }}</h2>
        <TagSelect mode="releaseNote" v-model="selectedFromReleaseNoteId" :searchParams="searchParams" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.toReleaseNote') }}</h2>
        <TagSelect mode="releaseNote" v-model="selectedToReleaseNoteId" :searchParams="searchParams" />
      </div>
    </div>
  </div>


</template>
