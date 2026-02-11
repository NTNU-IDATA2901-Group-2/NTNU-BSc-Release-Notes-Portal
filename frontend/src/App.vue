<script setup lang="ts">
import Header from './components/Header.vue';
</script>

<template>
  <div class="flex flex-col h-screen">
    <Header />
    <main class="flex-1">
      <RouterView class="w-full h-full"/>
    </main>
  </div>
  
</template>
