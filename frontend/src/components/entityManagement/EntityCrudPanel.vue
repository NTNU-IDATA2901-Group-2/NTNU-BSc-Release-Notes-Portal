<script lang="ts" setup>
import Button from '@/components/ui/button/Button.vue';
import Input from '@/components/ui/input/Input.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog';
import { PersistTagSchema } from '@/schemas';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { useI18n } from 'vue-i18n';
import { TableCell, Table, TableHeader, TableRow, TableHead, TableBody } from '@/components/ui/table';
import { FlexRender, getCoreRowModel, useVueTable, type ColumnDef } from '@tanstack/vue-table';
import { Pencil, Trash2 } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import DialogPrompt from '@/components/DialogPrompt.vue';
import type { Tag } from '@/utils/types';
import type { EntityCrudConfig } from './entity-crud-config';
import { computed, h, ref } from 'vue';
import axios from 'axios';

const props = defineProps<{ config: EntityCrudConfig }>();

const { t } = useI18n();

const entityLabel = computed(() => t(`title.${props.config.entityKey}`));

const form = useForm({
  validationSchema: toTypedSchema(PersistTagSchema),
});

const { isPending, isError, data: entities } = props.config.useList();

const createMutation = props.config.useCreate({
    onSuccess: () => {
        toast.success(t('entityManagement.createSuccess', { entity: entityLabel.value }));
        form.handleReset();
    },
    onError: () => {
        toast.error(t('entityManagement.createError', { entity: entityLabel.value }));
    }
});

const onSubmit = form.handleSubmit((values) => {
    createMutation.mutate(values);
});

const updateMutation = props.config.useUpdate({
    onSuccess: () => {
        toast.success(t('entityManagement.updateSuccess', { entity: entityLabel.value }));
    },
    onError: () => {
        toast.error(t('entityManagement.updateError', { entity: entityLabel.value }));
    }
});

const deleteMutation = props.config.useDelete({
    onSuccess: () => {
        toast.success(t('entityManagement.deleteSuccess', { entity: entityLabel.value }));
    },
    onError: (error?: unknown) => {
        // The backend answers 409 when the entity is still referenced by change or release notes.
        if (axios.isAxiosError(error) && error.response?.status === 409) {
            toast.error(t('entityManagement.deleteConflictError', { entity: entityLabel.value }));
        } else {
            toast.error(t('entityManagement.deleteError', { entity: entityLabel.value }));
        }
    }
});

const showEditDialog = ref(false);

const entityToEdit = ref<Tag | null>(null);

const editedName = ref('');

const openEditDialog = (entity: Tag) => {
    entityToEdit.value = entity;
    editedName.value = entity.name;
    showEditDialog.value = true;
}

const closeEditDialog = () => {
    showEditDialog.value = false;
    entityToEdit.value = null;
    editedName.value = '';
}

const onSaveEdit = () => {
    if (entityToEdit.value === null || !editedName.value.trim()) {
        return;
    }
    updateMutation.mutate({ id: entityToEdit.value.id, name: editedName.value.trim() });
    closeEditDialog();
}

const showConfirmDeletePrompt = ref(false);

const entityIdToDelete = ref<number | null>(null);

const onDeleteEntity = (id: number | null) => {
    if (id === null) {
        console.warn("Attempted to delete entity with null id");
    } else {
        deleteMutation.mutate(id);
    }
}

const columns = computed<ColumnDef<Tag>[]>(() => [
  {
    accessorKey: 'name',
    header: () => t('entityManagement.name'),
  },
  {
    id: 'actions',
    header: () => '',
    cell: ({ row }) =>
      h('div', { class: 'flex gap-2 justify-end' }, [
        h(Button, {
          variant: 'outline',
          size: 'sm',
          'aria-label': t('button.edit'),
          onClick: () => openEditDialog(row.original),
        }, () => h(Pencil)),
        h(Button, {
          variant: 'destructive',
          size: 'sm',
          'aria-label': t('button.delete'),
          onClick: () => { entityIdToDelete.value = row.original.id; showConfirmDeletePrompt.value = true; },
        }, () => h(Trash2)),
      ]),
  },
])

const table = useVueTable({
  get data() { return entities.value ?? [] },
  get columns() { return columns.value },
  getCoreRowModel: getCoreRowModel(),
})

</script>

<template>
    <DialogPrompt
        :open="showConfirmDeletePrompt"
        mode="delete"
        @update:open="showConfirmDeletePrompt = false"
        @cancel="showConfirmDeletePrompt = false; entityIdToDelete = null"
        @confirm="onDeleteEntity(entityIdToDelete)"
        :title-key="'deletePrompt.title'"
        :description-key="'deletePrompt.description'"
    />
    <Dialog :open="showEditDialog" @update:open="closeEditDialog">
        <DialogContent>
            <DialogHeader>
                <DialogTitle>{{ t('entityManagement.editTitle', { entity: entityLabel }) }}</DialogTitle>
                <DialogDescription>{{ t('entityManagement.editDescription') }}</DialogDescription>
            </DialogHeader>
            <Input v-model="editedName" :placeholder="t('entityManagement.namePlaceholder')" @keydown.enter="onSaveEdit" />
            <DialogFooter>
                <Button variant="outline" @click="closeEditDialog">{{ t('button.cancel') }}</Button>
                <Button variant="solidaccent" :disabled="!editedName.trim()" @click="onSaveEdit">{{ t('button.save') }}</Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>
    <div class="flex flex-col gap-8 w-full">
        <form class="flex flex-row flex-wrap gap-4 items-start" @submit.prevent="onSubmit">
            <FormField v-slot="{ componentField }" name="name">
                <FormItem class="relative">
                    <FormControl>
                        <Input v-bind="componentField" :placeholder="t('entityManagement.namePlaceholder')" />
                    </FormControl>
                    <FormMessage class="absolute top-[110%]" />
                </FormItem>
            </FormField>
            <Button class="self-end" type="submit" variant="outline">{{ t('entityManagement.add', { entity: entityLabel }) }}</Button>
        </form>
        <Spinner v-if="isPending" />
        <p v-if="isError">{{ t('entityManagement.loadError') }}</p>
        <div v-if="entities && !isPending && !isError" class="border rounded-md overflow-hidden">
            <Table class="overflow-hidden">
                <TableHeader>
                    <TableRow v-for="headerGroup in table.getHeaderGroups()" :key="headerGroup.id">
                        <TableHead v-for="header in headerGroup.headers" :key="header.id" class="whitespace-normal">
                            <FlexRender
                                v-if="!header.isPlaceholder" :render="header.column.columnDef.header"
                                :props="header.getContext()"
                            />
                        </TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    <template v-if="table.getRowModel().rows?.length">
                        <TableRow v-for="row in table.getRowModel().rows" :key="row.id">
                            <TableCell v-for="cell in row.getVisibleCells()" :key="cell.id" class="whitespace-normal wrap-break-word text-text-primary">
                                <FlexRender :render="cell.column.columnDef.cell" :props="cell.getContext()" />
                            </TableCell>
                        </TableRow>
                    </template>
                    <template v-else>
                        <TableRow>
                            <TableCell :colspan="columns.length" class="h-24 text-center">
                                {{ t('entityManagement.empty') }}
                            </TableCell>
                        </TableRow>
                    </template>
                </TableBody>
            </Table>
        </div>
    </div>
</template>
