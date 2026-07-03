<script setup lang="ts">
import { handleSigninCallback } from '@/utils/auth';
import { routeNames } from '@/utils/router';
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(async () => {
  try {
    await handleSigninCallback();
    router.replace(routeNames.releaseNotes);
  } catch (err) {
    console.error('Sign-in callback error:', err);
    router.replace(routeNames.signIn);
  }
});
</script>

<template>
    <div class="flex flex-col min-h-full justify-center items-center"/>
</template>
