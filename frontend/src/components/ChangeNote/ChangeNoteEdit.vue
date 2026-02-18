<script setup lang="ts">
import type { ChangeNote, PersistChangeNoteDTO } from '@/types';
import { Input } from '../ui/input';
import { Button } from '../ui/button';
import { Ban, Save } from 'lucide-vue-next';
import { Textarea } from '../ui/textarea';
import TagSelect from '../TagSelect.vue';
import { Separator } from '../ui/separator';
import { useForm } from 'vee-validate';
import { toTypedSchema } from '@vee-validate/zod';
import { EditChangeNoteSchema } from '@/schemas';
import { useMutation, useQueryClient } from '@tanstack/vue-query';
import { updateChangeNote } from '@/api/change-note-api';
import { toast } from 'vue-sonner';

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
    productId: props.changeNote.product.id,
    scopeId: props.changeNote.scope.id,
    featureId: props.changeNote.feature.id,
    customerId: props.changeNote.customer.id,
    developerNotes: props.changeNote.developerNotes,
    upgradeNotes: props.changeNote.upgradeNotes,
    changeSource: props.changeNote.changeSource,
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

const queryClient = useQueryClient();
const updateChangeNoteMutation = useMutation({
    mutationFn: (values: PersistChangeNoteDTO) => updateChangeNote(props.changeNote.id, values),
    onSuccess: () => {
        toast.success('Change note updated successfully');
        emit('update:modelValue', false);
        queryClient.invalidateQueries({ queryKey: ['changeNote', props.changeNote.id] });
    }
})

const onSubmit = handleSubmit((values : PersistChangeNoteDTO) => {
  updateChangeNoteMutation.mutate(values);
});

const onCancel = () => {
    emit('update:modelValue', false);
}

</script>

<template>

<form @submit="onSubmit">
    <div class="md:hidden flex w-full mt-4 justify-end gap-2">
    <Button type="button" @click="onCancel" variant="outline">Cancel
        <Ban />
    </Button>
    <Button type="submit" disabled variant="outline">Save
        <Save />
    </Button>
    </div>

    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
    <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
        <div class="flex items-center gap-4">
            <Input class="w-full" v-model="reference" placeholder="Reference" />
        </div>
        <div class="flex gap-4">
            <Button type="button" @click="onCancel" class="hidden md:flex" variant="outline">
                Cancel
            <Ban />
            </Button>
            <Button class="hidden md:flex" type="submit" variant="outline">Save
            <Save />
            </Button>
        </div>
        </div>

        <Textarea placeholder="Description of change" class="w-full" v-model="description"></Textarea>

        <div class="flex flex-wrap justify-between gap-4">
        <div class="flex flex-col gap-1">
            <h4 class="text-md">Product</h4>
            <TagSelect mode="product" v-model="productId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h4 class="text-md">Scope</h4>
            <TagSelect mode="scope" v-model="scopeId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h4 class="text-md">Feature</h4>
            <TagSelect mode="feature" v-model="featureId"/>
        </div>
        <div class="flex flex-col gap-1">
            <h4 class="text-md">Customer</h4>
            <TagSelect mode="customer" v-model="customerId"/>
        </div>
        </div>
    </div>

    <Separator class="w-full h-2" />

    <div class="flex flex-col w-full text-xl gap-10">
        <div>
        <h3 class="text-lg">Developer Notes</h3>
        <Textarea placeholder="Developer notes" class="w-full" v-model="developerNotes"></Textarea>
        </div>
        <div>
        <h3 class="text-lg">Upgrade Notes</h3>
        <Textarea placeholder="Upgrade notes" class="w-full" v-model="upgradeNotes"></Textarea>
        </div>
    </div>
    </div>
</form>

</template>