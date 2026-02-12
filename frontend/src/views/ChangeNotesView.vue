<script setup lang="ts">
import ChangeNoteCard from '@/components/ChangeNoteCard.vue';

</script>

<template>
  <div class="flex w-full justify-center">
    <div class="flex flex-col gap-4 max-w-3xl">
      <h1>Change Notes</h1>
      <ChangeNoteCard
        v-for="index in 10"
        :key="index"
        title="Test"
        content="Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum  feugiat ornare leo non pharetra. Cras rutrum ultricies erat, eget  commodo quam porta ut. Quisque placerat metus vel consequat tempor.  Aliquam ac finibus mi, ac luctus lacus. Phasellus laoreet molestie  lectus. Nullam vel arcu eu arcu euismod commodo. Phasellus cursus,  tortor id interdum commodo, purus tortor placerat eros, a lacinia tortor sem a dui. Vestibulum eu congue orci, non lacinia nisi. Quisque viverra viverra ipsum ac tempor. Mauris finibus tincidunt dapibus."
        :published="true"
        company="HIG"
        scope="Minor"
        feature="Pick Planning"
      />
    </div>
  </div>
</template>