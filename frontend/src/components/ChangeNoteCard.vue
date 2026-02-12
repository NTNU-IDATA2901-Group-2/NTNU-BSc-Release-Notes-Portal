<script setup lang="ts">
    import type { PrimitiveProps } from 'reka-ui';
    import Badge from './ui/badge/Badge.vue';

    const props = defineProps<PrimitiveProps & {
        title: string,
        content: string,
        published: boolean,
        scope?: string,
        company?: string,
        feature?: string,
    }>()
</script>

<template>
    <div class="flex flex-col p-4 gap-2 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
        <div class="flex justify-between">
            <div class="flex gap-2">
              <h3 class="text-xl">{{ props.title }}</h3>
              <Badge v-if="props.company" :variant="'outline'">{{ props.company }}</Badge>
            </div>
            <Badge :variant="props.published ? 'success' : 'destructive'">{{ props.published ? 'Published' : 'Private' }}</Badge>
        </div>
        <p>{{ props.content }}</p>
        <div class="flex flex-row gap-2">
            <Badge v-if="props.scope" :variant="'default'">{{ props.scope }}</Badge>
            <Badge v-if="props.feature" :variant="'default'">{{ props.feature }}</Badge>
        </div>
    </div>
</template>