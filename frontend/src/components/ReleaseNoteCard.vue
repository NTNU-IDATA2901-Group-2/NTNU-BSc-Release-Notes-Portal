<script setup lang="ts">
    import type { PrimitiveProps } from 'reka-ui';
    import Badge from './ui/badge/Badge.vue';


    const props = defineProps<PrimitiveProps & {
        title: string,
        content: string,
        published: boolean,
    }>()
</script>

<template>
    <div class="flex flex-col p-4 gap-2 h-40 overflow-hidden text-wrap rounded-lg hover:bg-text-primary/10 transition-colors">
        <div class="flex flex-row justify-between">
            <h3 class="text-xl">{{ props.title }}</h3>
            <Badge :variant="props.published ? 'success' : 'destructive'">{{ props.published ? 'Published' : 'Private' }}</Badge>
        </div>
        <p>{{ props.content }}</p>
    </div>
</template>