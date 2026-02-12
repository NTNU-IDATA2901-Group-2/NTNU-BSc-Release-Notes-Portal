<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';
import { ArrowLeft, EllipsisVertical  } from "lucide-vue-next"
import Separator from '@/components/ui/separator/Separator.vue';
import type { ReleaseNote } from '@/types';
import Badge from '@/components/ui/badge/Badge.vue';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useTheme } from '@/utils/theme';
import { useReleaseNote } from '@/api/release-note-api';
import Spinner from '@/components/ui/spinner/Spinner.vue';


  const { theme } = useTheme()

  const route = useRoute();

  const id = route.params.id as string;
  const { isPending, isFetching, isError, data: releaseNote } = useReleaseNote(id);
</script>

<template>
  <div class="flex flex-col items-center px-4 mb-20">
    <Button variant="outline" class="mb-4 absolute left-10 mt-10" @click="$router.push('/')"><ArrowLeft />Previous</Button>
    <Spinner v-if="isPending || isFetching" />

    <div v-if="!isPending && !isFetching && !isError && releaseNote" class="flex flex-col gap-16 flex-1 w-full items-center mt-24 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <h1 v-if="releaseNote" class="text-2xl">Release Note {{ releaseNote.version }}</h1>
            <Badge v-if="releaseNote" class="h-6" :variant="releaseNote.published ? 'success' : 'destructive'">{{ releaseNote.published ? 'Published' : 'Private' }}</Badge>
          </div>
          	<DropdownMenu >
              <DropdownMenuTrigger class="ml-auto cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                <EllipsisVertical class="text-text-primary"/>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="mr-8 mt-2">
                  <DropdownMenuItem @click="theme = theme === 'dark' ? 'light' : 'dark'">
                <div class="w-full flex gap-2">
                    <p class="text-text-dark-static ml-auto">Toggle theme</p>
                    <SunMoon class="text-text-dark-static"/>
                </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                <div class="w-full flex gap-2">
                    <p class="ml-auto text-text-dark-static">Sign out</p>
                    <LogOut class="text-text-dark-static"/>
                </div>
                  </DropdownMenuItem>
              </DropdownMenuContent>
	          </DropdownMenu>
          </div>

          <p v-if="releaseNote" class="">{{ releaseNote.description }}</p>
      </div>
      <Separator class="w-full h-2"/>
      <div class="flex flex-col gap-4 w-full text-xl">
        <h2>Changes</h2>
        <div 
            v-for="change in releaseNote.changeNotes"
            :key="change.id" 
            class="flex flex-col gap-2"
            >
          <h3 class="text-lg">{{ change.reference }}</h3>
          <p class="text-sm">{{ change.description }}</p>
        </div>
      </div>
      <div class="flex flex-col gap-4 w-full text-xl">
        <h2>Developer Notes</h2>
        <div
            v-for="change in releaseNote.changeNotes"
            :key="change.id"
            class="flex flex-col gap-2"
            >
          <h3 class="text-lg">{{ change.reference }}</h3>
          <p class="text-sm">{{ change.developerNotes }}</p>
        </div>
      </div>
      <div class="flex flex-col gap-4 w-full text-xl">
        <h2>Upgrade Requirements</h2>
        <div
            v-for="change in releaseNote.changeNotes"
            :key="change.id"
            class="flex flex-col gap-2"
            >
          <h3 class="text-lg">{{ change.reference }}</h3>
          <p class="text-sm">{{ change.upgradeNotes }}</p>
        </div>
      </div>
    </div>
  </div>
</template>