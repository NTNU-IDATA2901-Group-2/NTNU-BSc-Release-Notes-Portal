<script lang="ts" setup>
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import EntityCrudPanel from '@/components/entityManagement/EntityCrudPanel.vue';
import { entityCrudConfigs } from '@/components/entityManagement/entity-crud-config';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
    <div class="flex flex-col items-center my-20 w-full">
        <div class="flex flex-col gap-8 w-full max-w-5xl px-4">
            <h1 class="text-xl">{{ t('entityManagement.title') }}</h1>
            <Tabs default-value="products">
                <TabsList>
                    <TabsTrigger v-for="config in entityCrudConfigs" :key="config.tabKey" :value="config.tabKey">
                        {{ t(`entityManagement.tabs.${config.tabKey}`) }}
                    </TabsTrigger>
                </TabsList>
                <TabsContent v-for="config in entityCrudConfigs" :key="config.tabKey" :value="config.tabKey" class="mt-4">
                    <EntityCrudPanel :config="config" />
                </TabsContent>
            </Tabs>
        </div>
    </div>
</template>
