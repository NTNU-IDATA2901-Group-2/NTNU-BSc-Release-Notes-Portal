<script lang="ts" setup>
import Button from '@/components/ui/button/Button.vue';
import Input from '@/components/ui/input/Input.vue';
import { FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form';
import { CreateGitRepositorySchema } from '@/schemas';
import { toTypedSchema } from '@vee-validate/zod';
import { useForm } from 'vee-validate';
import { useI18n } from 'vue-i18n';
import { TableCell, Table, TableHeader, TableRow, TableHead, TableBody } from '@/components/ui/table';
import { RefreshCw, Trash2 } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import { useDeleteGitRepository, useGetGitRepositories, usePersistGitRepository, useSyncAllRepositories, useSyncRepository } from '@/api/git-repository-api';

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
    console.log(values);
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

const onDeleteRepository = (id: number) => {
    deleteGitRepositoryMutation.mutate(id);
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

</script>

<template>
    <div class="flex flex-col items-center mt-20">
        <div class="flex flex-col gap-8">
            <div class="flex flex-row justify-between">
                <h1 class="text-xl">{{ t('repositories.title') }}</h1>
                <Button variant="solidaccent" @click="syncAllRepositoriesMutation.mutate()">{{ t('repositories.syncAll') }}</Button>
            </div>
            <form class="flex flex-row gap-4" @submit.prevent="onSubmit">
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
                <Button class="self-end" type="submit" variant="outline">{{ t('repositories.addRepository') }}</Button>
            </form>
            <p v-if="isError">{{ t('repositories.error') }}</p>
            <Table v-if="gitRepositories && !isPending && !isError">
                <TableHeader>
                <TableRow>
                    <TableHead>
                    {{ t('repositories.id') }}
                    </TableHead>
                    <TableHead>
                    {{ t('repositories.name') }}
                    </TableHead>
                    <TableHead>
                    {{ t('repositories.url') }}
                    </TableHead>
                    <TableHead>
                    {{ t('repositories.sync') }}
                    </TableHead>
                    <TableHead class="text-right">
                    {{ t('repositories.remove') }}
                    </TableHead>
                </TableRow>
                </TableHeader>
                <TableBody>
                <TableRow v-for="repo in gitRepositories" :key="repo.id">
                    <TableCell class="font-medium text-text-primary">{{ repo.id }}</TableCell>
                    <TableCell class="text-text-primary">{{ repo.name }}</TableCell>
                    <TableCell class="text-text-primary">{{ repo.url }}</TableCell>
                    <TableCell class="text-text-primary text-center">
                        <Button class="self-center" variant="outline" size="sm" @click="onSyncRepository(repo.id)">
                            <RefreshCw />
                        </Button>
                    </TableCell>
                    <TableCell class="text-text-primary text-center">
                        <Button class="self-right" variant="destructive" size="sm" @click="onDeleteRepository(repo.id)">
                            <Trash2 />
                        </Button>
                    </TableCell>
                </TableRow>
                </TableBody>
            </Table>
        </div>
    </div>
</template>