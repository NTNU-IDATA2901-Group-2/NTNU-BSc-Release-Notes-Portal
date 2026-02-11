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

  const { theme } = useTheme()

  const route = useRoute();
  const id = route.params.id;

  const releaseNote: ReleaseNote = {
    id: 1,
    version: "1.0.0",
    description: "This is the description of release note 1.",
    changeNotes: [
      {
        id: 1,
        reference: "CHG-001",
        description: "This is the description of change note 1.",
        developerNotes: "These are the developer notes for change note 1.",
        upgradeNotes: "These are the upgrade notes for change note 1.",
        changeSource: "Internal",
        product: { id: 1, name: "Product A" },
        scope: { id: 1, name: "Scope A" },
        feature: { id: 1, name: "Feature A" },
        customer: { id: 1, name: "Customer A" },
        published: true,
        archived: false
      },
      {
        id: 2,
        reference: "CHG-002",
        description: "This is the description of change note 2.",
        developerNotes: "These are the developer notes for change note 2.",
        upgradeNotes: "These are the upgrade notes for change note 2.",
        changeSource: "External",
        product: { id: 2, name: "Product B" },
        scope: { id: 2, name: "Scope B" },
        feature: { id: 2, name: "Feature B" },
        customer: { id: 2, name: "Customer B" },
        published: true,
        archived: false
      }    
    ],
    published: true
  }


</script>

<template>
  <div class="flex flex-col items-center px-4">
    <Button variant="outline" class="mb-4 fixed left-10 mt-10" @click="$router.push('/')"><ArrowLeft />Previous</Button>

    <div class="flex flex-col gap-16 flex-1 w-full items-center mt-24 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <h1 class="text-2xl">Release Note {{ releaseNote.version }}</h1>
            <Badge class="h-6" :variant="releaseNote.published ? 'success' : 'destructive'">{{ releaseNote.published ? 'Published' : 'Private' }}</Badge>
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

          <p class="">{{ releaseNote.description }}</p>
        <div class="flex gap-4">
          <Badge class="w-max" variant="default">{{ releaseNote.changeNotes[0].product.name }}</Badge>
          <Badge class="w-max" variant="default">{{ releaseNote.changeNotes[0].product.name }}</Badge>
        </div>
      </div>
      <Separator class="w-full h-2"/>
      <div>
        <h2>Changes</h2>
      </div>
      <div>
        <h2>Developer Notes</h2>
      </div>
      <div>
        <h2>Upgrade Requirements</h2>
      </div>
    </div>
  </div>
</template>