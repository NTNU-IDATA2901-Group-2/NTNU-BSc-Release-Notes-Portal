<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { ArrowLeft } from 'lucide-vue-next';

  
</script>

<template>
  <div class="flex flex-col min-h-full justify-center items-center relative">
    <Button variant="outline" class="w-30 absolute top-4 left-4" @click="$router.back()"><ArrowLeft />Previous</Button>
    <div class="flex flex-col md:flex-row items-center gap-8">
      <img class="w-60 md:w-80 lg:w-100" src="../assets/not_found.svg"/>
      <div class="w-60 md:w-80 lg:w-100 flex flex-col gap-4">
        <h1 class="text-2xl self-center">404 Not Found</h1>
        <p>The resource you are trying to access has either been moved, or does not exist.</p>
      </div>
    </div>
  </div>
</template>