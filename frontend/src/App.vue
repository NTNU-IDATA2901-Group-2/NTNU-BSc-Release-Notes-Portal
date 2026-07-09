<script setup lang="ts">
// import Footer from './components/AppFooter.vue';
import Header from './components/AppHeader.vue';
import { Toaster } from './components/ui/sonner';
import TooltipProvider from './components/ui/tooltip/TooltipProvider.vue';

</script>

<template>
  <TooltipProvider>
    <div class="flex flex-col min-h-screen">
      <Toaster position="bottom-center" />
      <Header />
      <main class="flex flex-1">
        <RouterView class="w-full flex-1"/>
      </main>
    </div>
    <!-- <Footer /> -->
  </TooltipProvider>
</template>
