<script setup lang="ts">

import Button from './components/ui/button/Button.vue';
import { routeNames } from './utils/router';
import { useTheme } from './utils/theme';

const { theme } = useTheme()
</script>

<template>
  <!--
  <Button variant="outline" @click="theme = theme === 'dark' ? 'light' : 'dark'">
    Toggle theme
  </Button>
  -->
  <nav>
    <RouterLink :to="routeNames.releaseNotes">Go to Release Notes</RouterLink>
    <RouterLink :to="routeNames.changeNotes">Go to Change Notes</RouterLink>
  </nav>
  <main>
    <RouterView />
  </main>
</template>