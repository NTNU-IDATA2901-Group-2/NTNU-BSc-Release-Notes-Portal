<script lang="ts" setup>
import Button from '@/components/ui/button/Button.vue';
import Input from '@/components/ui/input/Input.vue';
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { Badge } from '@/components/ui/badge';
import { FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog';
import { CreateGitRepositorySchema } from '@/schemas';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { useI18n } from 'vue-i18n';
import { TableCell, Table, TableHeader, TableRow, TableHead, TableBody } from '@/components/ui/table';
import { FlexRender, getCoreRowModel, useVueTable, type ColumnDef } from '@tanstack/vue-table';
import { KeyRound, RefreshCw, Trash2 } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import { useDeleteGitRepository, useGetGitRepositories, usePersistGitRepository, useSyncAllRepositories, useSyncRepository, useUpdateGitRepositoryPat } from '@/api/git-repository-api';
import DialogPrompt from '@/components/DialogPrompt.vue';
import type { GitRepository } from '@/utils/types';
import { computed, h, ref } from 'vue';

const { t } = useI18n();

const form = useForm({
  validationSchema: toTypedSchema(CreateGitRepositorySchema),
});

const {isPending, isError,  data: gitRepositories } = useGetGitRepositories();

const createGitRepositoryMutation = usePersistGitRepository({
    onSuccess: () => {
        toast.success(t('repositories.createSuccess'));
        form.handleReset();
    },
    onError: () => {
        toast.error(t('repositories.createError'));
    }
})

const onSubmit = form.handleSubmit((values) => {
    createGitRepositoryMutation.mutate(values);
})

const deleteGitRepositoryMutation = useDeleteGitRepository({
    onSuccess: () => {
        toast.success(t('repositories.deleteSuccess'));
    },
    onError: () => {
        toast.error(t('repositories.deleteError'));
    }
})

const onDeleteRepository = (id: number | null) => {
    if (id === null) {
        console.warn("Attempted to delete Git repository with null id");
    } else {
        deleteGitRepositoryMutation.mutate(id);
    }
}

const syncAllRepositoriesMutation = useSyncAllRepositories({
    onSuccess: () => {
        toast.success(t('repositories.syncSuccess'));
    },
    onError: () => {
        toast.error(t('repositories.syncError'));
    }
})

const syncGitRepositoryMutation = useSyncRepository({
    onSuccess: () => {
        toast.success(t('repositories.syncSuccess'));
    },
    onError: () => {
        toast.error(t('repositories.syncError'));
    }
})

const onSyncRepository = (id: number) => {
    syncGitRepositoryMutation.mutate(id);
}

const isSyncingAll = computed(() => syncAllRepositoriesMutation.isPending.value);

const isSyncingRepository = (id: number) =>
    syncGitRepositoryMutation.isPending.value && syncGitRepositoryMutation.variables.value === id;

const updateGitRepositoryPatMutation = useUpdateGitRepositoryPat({
    onSuccess: () => {
        toast.success(t('repositories.updatePatSuccess'));
    },
    onError: () => {
        toast.error(t('repositories.updatePatError'));
    }
})

const showConfirmDeletePrompt = ref(false);

const repositoryIdToDelete = ref<number | null>(null);

const showUpdatePatPrompt = ref(false);

const repositoryIdToUpdatePat = ref<number | null>(null);

const newPat = ref('');

const openUpdatePatPrompt = (id: number) => {
    repositoryIdToUpdatePat.value = id;
    newPat.value = '';
    showUpdatePatPrompt.value = true;
}

const closeUpdatePatPrompt = () => {
    showUpdatePatPrompt.value = false;
    repositoryIdToUpdatePat.value = null;
    newPat.value = '';
}

const onUpdatePat = () => {
    if (repositoryIdToUpdatePat.value === null || !newPat.value.trim()) {
        return;
    }
    updateGitRepositoryPatMutation.mutate({ id: repositoryIdToUpdatePat.value, pat: newPat.value.trim() });
    closeUpdatePatPrompt();
}

const columns = computed<ColumnDef<GitRepository>[]>(() => [
  {
    accessorKey: 'name',
    header: () => t('repositories.name'),
  },
  {
    accessorKey: 'url',
    header: () => t('repositories.url'),
  },
  {
    accessorKey: 'changeNoteDirectory',
    header: () => t('repositories.changeNoteDirectory'),
  },
  {
    accessorKey: 'patSet',
    header: () => t('repositories.pat'),
    cell: ({ row }) =>
      h('div', { class: 'flex gap-2 items-center' }, [
        h(Badge, { variant: row.original.patSet ? 'success' : 'destructive' },
          () => row.original.patSet ? t('repositories.patSet') : t('repositories.patNotSet')),
        h(Button, {
          variant: 'outline',
          size: 'sm',
          'aria-label': t('repositories.updatePat'),
          onClick: () => openUpdatePatPrompt(row.original.id),
        }, () => h(KeyRound)),
      ]),
  },
  {
    id: 'actions',
    header: () => '',
    cell: ({ row }) =>
      h('div', { class: 'flex gap-2 justify-end' }, [
        h(Button, {
          variant: 'outline',
          size: 'sm',
          'aria-label': t('repositories.sync'),
          disabled: isSyncingRepository(row.original.id),
          onClick: () => onSyncRepository(row.original.id),
        }, () => isSyncingRepository(row.original.id) ? h(Spinner, { class: 'size-4 my-0' }) : h(RefreshCw)),
        h(Button, {
          variant: 'destructive',
          size: 'sm',
          'aria-label': t('repositories.remove'),
          onClick: () => { repositoryIdToDelete.value = row.original.id; showConfirmDeletePrompt.value = true; },
        }, () => h(Trash2)),
      ]),
  },
])

const table = useVueTable({
  get data() { return gitRepositories.value ?? [] },
  get columns() { return columns.value },
  getCoreRowModel: getCoreRowModel(),
})

</script>

<template>
    <DialogPrompt
        :open="showConfirmDeletePrompt"
        mode="delete"
        @update:open="showConfirmDeletePrompt = false"
        @cancel="showConfirmDeletePrompt = false; repositoryIdToDelete = null"
        @confirm="onDeleteRepository(repositoryIdToDelete)"
        :title-key="'repositories.deleteTitle'"
        :description-key="'repositories.deleteDescription'"
    />
    <Dialog :open="showUpdatePatPrompt" @update:open="closeUpdatePatPrompt">
        <DialogContent>
            <DialogHeader>
                <DialogTitle>{{ t('repositories.updatePatTitle') }}</DialogTitle>
                <DialogDescription>{{ t('repositories.updatePatDescription') }}</DialogDescription>
            </DialogHeader>
            <Input v-model="newPat" type="password" :placeholder="t('repositories.patPlaceholder')" @keydown.enter="onUpdatePat" />
            <DialogFooter>
                <Button variant="outline" @click="closeUpdatePatPrompt">{{ t('button.cancel') }}</Button>
                <Button variant="solidaccent" :disabled="!newPat.trim()" @click="onUpdatePat">{{ t('repositories.updatePat') }}</Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>
    <div class="flex flex-col items-center mt-20 w-full">
        <div class="flex flex-col gap-8 w-full max-w-5xl px-4">
            <div class="flex flex-row justify-between">
                <h1 class="text-xl">{{ t('repositories.title') }}</h1>
                <Button variant="solidaccent" :disabled="isSyncingAll" @click="syncAllRepositoriesMutation.mutate()">
                    {{ t('repositories.syncAll') }}
                    <Spinner v-if="isSyncingAll" class="size-4 my-0" />
                </Button>
            </div>
            <form class="flex flex-row flex-wrap gap-4 items-start" @submit.prevent="onSubmit">
                <FormField v-slot="{ componentField }" name="name">
                    <FormItem class="relative">
                        <p class="mb-1">{{ t('repositories.name') }}</p>
                        <FormControl>
                            <Input v-bind="componentField" :placeholder="t('repositories.namePlaceholder')" />
                        </FormControl>
                        <FormMessage class="absolute top-[110%]" />
                    </FormItem>
                </FormField>
                <FormField v-slot="{ componentField }" name="url">
                    <FormItem class="relative">
                        <p class="mb-1">{{ t('repositories.url') }}</p>
                        <FormControl>
                            <Input v-bind="componentField" :placeholder="t('repositories.urlPlaceholder')" />
                        </FormControl>
                        <FormMessage class="absolute top-[110%]"/>
                    </FormItem>
                </FormField>
                <FormField v-slot="{ componentField }" name="changeNoteDirectory">
                    <FormItem class="relative">
                        <p class="mb-1">{{ t('repositories.changeNoteDirectory') }}</p>
                        <FormControl>
                            <Input v-bind="componentField" :placeholder="t('repositories.changeNoteDirectoryPlaceholder')" />
                        </FormControl>
                        <FormMessage class="absolute top-[110%]"/>
                    </FormItem>
                </FormField>
                <FormField v-slot="{ componentField }" name="pat">
                    <FormItem class="relative">
                        <p class="mb-1">{{ t('repositories.pat') }}</p>
                        <FormControl>
                            <Input v-bind="componentField" type="password" :placeholder="t('repositories.patPlaceholder')" />
                        </FormControl>
                        <FormMessage class="absolute top-[110%]"/>
                    </FormItem>
                </FormField>
                <Button class="self-end" type="submit" variant="outline">{{ t('repositories.addRepository') }}</Button>
            </form>
            <p v-if="isError">{{ t('repositories.error') }}</p>
            <div v-if="gitRepositories && !isPending && !isError" class="border rounded-md overflow-hidden">
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
                                    {{ t('repositories.noGitRepositories') }}
                                </TableCell>
                            </TableRow>
                        </template>
                    </TableBody>
                </Table>
            </div>
        </div>
    </div>
</template>
