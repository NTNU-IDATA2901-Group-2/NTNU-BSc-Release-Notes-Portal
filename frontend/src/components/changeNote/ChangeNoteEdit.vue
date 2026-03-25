<script setup lang="ts">
import type { ChangeNote, PersistChangeNoteDTO } from '@/utils/types';
import { Input } from '../ui/input';
import { Button } from '../ui/button';
import { Ban, Save } from 'lucide-vue-next';
import { Textarea } from '../ui/textarea';
import TagSelect from '../TagSelect.vue';
import { Separator } from '../ui/separator';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { EditChangeNoteSchema } from '@/schemas';
import { useUpdateChangeNote } from '@/api/change-note-api';
import { toast } from 'vue-sonner';
import { router } from '@/utils/router';
import { useI18n } from 'vue-i18n';
import { useSummarizeChangeNote } from '@/api/ai-api';

const props = defineProps<{
  changeNote: ChangeNote;
  modelValue?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>();

const { handleSubmit, defineField } = useForm({
  validationSchema: toTypedSchema(EditChangeNoteSchema),
  initialValues: {
    reference: props.changeNote.reference,
    description: props.changeNote.description,
    productId: props.changeNote.product?.id,
    scopeId: props.changeNote.scope?.id,
    featureId: props.changeNote.feature?.id,
    customerId: props.changeNote.customer?.id,
    developerNotes: props.changeNote.developerNotes,
    upgradeNotes: props.changeNote.upgradeNotes,
  }
});

const [reference] = defineField('reference');
const [description] = defineField('description');
const [productId] = defineField('productId');
const [scopeId] = defineField('scopeId');
const [featureId] = defineField('featureId');
const [customerId] = defineField('customerId');
const [developerNotes] = defineField('developerNotes');
const [upgradeNotes] = defineField('upgradeNotes');

const { t } = useI18n();

const updateChangeNoteMutation = useUpdateChangeNote({
    onSuccess: () => {
        toast.success(t('toast.changeNoteUpdatedSuccess'));
        emit('update:modelValue', false);
        router.push(`/change-notes/${props.changeNote.id}`);   
    },
    onError: () => {
        toast.error(t('toast.changeNoteUpdateError'));
    }
})

const onSubmit = handleSubmit((values : PersistChangeNoteDTO) => {
    values.productId = values.productId === -1 ? undefined : values.productId;
    values.scopeId = values.scopeId === -1 ? undefined : values.scopeId;
    values.featureId = values.featureId === -1 ? undefined : values.featureId;
    values.customerId = values.customerId === -1 ? undefined : values.customerId;
  updateChangeNoteMutation.mutate({ id: props.changeNote.id, dto: values });
});

const onCancel = () => {
    emit('update:modelValue', false);
    router.push(`/change-notes/${props.changeNote.id}`);
}

const summarizeChangeNote = useSummarizeChangeNote({
    onSuccess: (summary?: string) => {
        description.value = summary;
        toast.success(t('toast.summarizeSuccess'));
    },
    onError: () => {
        toast.error(t('toast.summarizeError'));
    }
})

</script>

<template>

<form @submit="onSubmit">
    <div class="md:hidden flex w-full mt-4 justify-end gap-2">
    <Button type="button" @click="onCancel" variant="outline">{{ t('button.cancel') }}
        <Ban />
    </Button>
    <Button type="submit" variant="outline">{{ t('button.save') }}
        <Save />
    </Button>
    </div>

    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-16 lg:w-4xl md:mt-42">
    <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
        <div class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.title') }}</h4>
            <Input class="w-full" v-model="reference" :placeholder="t('placeholder.title')" />
        </div>
        <div class="flex gap-4">
            <Button type="button" @click="onCancel" class="hidden md:flex" variant="outline">
                {{ t('button.cancel') }}
            <Ban />
            </Button>
            <Button class="hidden md:flex" type="submit" variant="outline">{{ t('button.save') }}
            <Save />
            </Button>
        </div>
        </div>

        <div class="flex flex-col gap-1">
            <div class = "flex flex-row justify-between">
                <h4 class="text-md">{{ t('title.description') }}</h4>
                <Button type="button" @click="summarizeChangeNote.mutate(props.changeNote.id)" variant="outline">Summarize</Button>
            </div>
            <Textarea :placeholder="t('placeholder.description')" class="w-full" v-model="description"></Textarea>
        </div>

        <div class="flex flex-wrap justify-between gap-4">
        <div class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.product') }}</h4>
            <TagSelect mode="product" v-model="productId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.scope') }}</h4>
            <TagSelect mode="scope" v-model="scopeId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.feature') }}</h4>
            <TagSelect mode="feature" v-model="featureId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h4 class="text-md">{{ t('title.customer') }}</h4>
            <TagSelect mode="customer" v-model="customerId"/>
        </div>
        </div>
    </div>

    <Separator class="w-full h-2" />

    <div class="flex flex-col w-full gap-10">
        <div class="flex flex-col gap-1">
        <h4 class="text-md">{{ t('title.developerNotes') }}</h4>
        <Textarea :placeholder="t('placeholder.developerNotes')" class="w-full" v-model="developerNotes"></Textarea>
        </div>
        <div class="flex flex-col gap-1">
        <h4 class="text-md">{{ t('title.upgradeRequirements') }}</h4>
        <Textarea :placeholder="t('placeholder.upgradeRequirements')" class="w-full" v-model="upgradeNotes"></Textarea>
        </div>
    </div>
    </div>
</form>

</template>