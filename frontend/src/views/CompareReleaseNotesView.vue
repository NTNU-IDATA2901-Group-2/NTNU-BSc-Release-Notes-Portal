<script setup lang="ts">
import TagSelect from '@/components/TagSelect.vue';
import { computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { CompareReleaseNotesSchema as CompareReleaseNotesSchema } from '@/schemas';
import Button from '@/components/ui/button/Button.vue';
import { useCompareReleaseNotes } from '@/api/release-note-api';

const { t } = useI18n();

const { defineField, handleSubmit } = useForm({
  validationSchema: toTypedSchema(CompareReleaseNotesSchema),
});

const [productId] = defineField('productId');
const [releaseNoteOneId] = defineField('releaseNoteOneId');
const [releaseNoteTwoId] = defineField('releaseNoteTwoId');

const noProductSelected = computed(
  () => productId.value === -1 || productId.value === undefined
)

const searchParams = computed(() =>
  noProductSelected.value ? undefined : { productIds: String(productId.value) }
)

const { mutate: compare, data: comparedReleaseNotes, reset } = useCompareReleaseNotes()

watch(productId, () => {
  releaseNoteOneId.value = undefined
  releaseNoteTwoId.value = undefined
  reset()
})

const onSubmit = handleSubmit((values) => {
  compare({ releaseNoteOneId: values.releaseNoteOneId, releaseNoteTwoId: values.releaseNoteTwoId })
});

</script>

<template>
  <div class="flex flex-col items-center mt-20 w-full">
    <form class="flex flex-row gap-8" @submit="onSubmit">
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.product') }}</h2>
        <TagSelect mode="product" v-model="productId" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.releaseNoteOne') }}</h2>
        <TagSelect mode="releaseNote" v-model="releaseNoteOneId" :search-params="searchParams" :disabled="noProductSelected" />
      </div>
      <div class="flex flex-col gap-2">
        <h2 class="text-lg">{{ t('compareReleaseNotes.releaseNoteTwo') }}</h2>
        <TagSelect mode="releaseNote" v-model="releaseNoteTwoId" :search-params="searchParams" :disabled="noProductSelected" />
      </div>
      <Button type="submit" class="mt-auto" variant="solidaccent" :disabled="!productId || !releaseNoteOneId || !releaseNoteTwoId">
        {{ t('compareReleaseNotes.compare') }}
      </Button>
    </form>

    <ul v-if="comparedReleaseNotes && comparedReleaseNotes.length" class="mt-6 flex flex-col gap-1">
      <li v-for="releaseNote in comparedReleaseNotes" :key="releaseNote.id">{{ releaseNote.tag }}</li>
    </ul>
  </div>


</template>
