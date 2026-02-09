<script setup lang="ts">

import { routeNames } from './router';

</script>

<template>
  <nav>
    <RouterLink :to="routeNames.releaseNotes">Go to Release Notes</RouterLink>
    <RouterLink :to="routeNames.changeNotes">Go to Change Notes</RouterLink>
  </nav>
  <main>
    <RouterView />
  </main>
</template>
