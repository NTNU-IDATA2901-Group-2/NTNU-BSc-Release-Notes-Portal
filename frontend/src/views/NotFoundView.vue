<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { ArrowLeft } from 'lucide-vue-next';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
  
</script>

<template>
  <div class="flex flex-col min-h-full justify-center items-center relative">
    <Button variant="outline" class="w-30 absolute top-4 left-4" @click="$router.back()"><ArrowLeft />{{ t('button.previous') }}</Button>
    <div class="flex flex-col md:flex-row items-center gap-8">
      <img class="w-60 md:w-80 lg:w-100" src="../assets/not_found.svg" alt="Content not found"/>
      <div class="w-60 md:w-80 lg:w-100 flex flex-col gap-4">
        <h1 class="text-2xl self-center">{{ t('notFound.404') }}</h1>
        <p>{{ t('notFound.resourceNotFound') }}</p>
      </div>
    </div>
  </div>
</template>