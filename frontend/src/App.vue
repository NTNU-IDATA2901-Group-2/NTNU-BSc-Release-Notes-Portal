<script setup lang="ts">
import Footer from './components/Footer.vue';
import Header from './components/Header.vue';
import { Toaster } from './components/ui/sonner';

</script>

<template>
  <div class="flex flex-col min-h-screen">
    <Toaster position="bottom-center" />
    <Header />
    <main class="flex flex-1">
      <RouterView class="w-full flex-1"/>
    </main>
  </div>
  <Footer />
</template>
