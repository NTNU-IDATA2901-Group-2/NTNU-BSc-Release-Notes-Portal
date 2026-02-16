<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';

import { ArrowLeft } from "lucide-vue-next"

  const route = useRoute();
  const id = route.params.id;
  
</script>

<template>
  <div class="flex flex-col items-center bg-amber-100 px-4">
    <Button variant="outline" class="mb-4 fixed left-10 mt-10" @click="$router.back()"><ArrowLeft />Previous</Button>

    <div class="flex flex-col flex-1 w-full bg-amber-200 items-center mt-24 mx-4 border-border border lg:w-4xl md:mt-42">
    </div>
  </div>
</template>