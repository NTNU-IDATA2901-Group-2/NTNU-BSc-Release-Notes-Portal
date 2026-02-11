<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';

import { ArrowLeft } from "lucide-vue-next"

  const route = useRoute();
  const id = route.params.id;
  
</script>

<template>
  <div class="flex">
    <Button variant="outline" class="mb-4" @click="$router.push('/')"><ArrowLeft /> Previous</Button>
    <h1>Release Note {{ id }}</h1>
  </div>
</template>