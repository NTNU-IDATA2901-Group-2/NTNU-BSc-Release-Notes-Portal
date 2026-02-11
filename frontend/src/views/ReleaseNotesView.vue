<script setup lang="ts">
import ReleaseNoteCard from '@/components/ReleaseNoteCard.vue';


</script>

<template>
    <h1>Release Notes</h1>
    <ReleaseNoteCard title="Test" content="This is a test release note." :published="true" />
</template>