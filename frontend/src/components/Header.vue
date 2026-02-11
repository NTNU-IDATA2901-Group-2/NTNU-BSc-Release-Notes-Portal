<script setup lang="ts">

import { routeNames } from '../utils/router';
import logoSvg from '../assets/solwr_logo.svg';
import Avatar from './ui/avatar/Avatar.vue';
import Separator from './ui/separator/Separator.vue';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

import { SunMoon } from "lucide-vue-next"
import { LogOut } from "lucide-vue-next"
import { useTheme } from '@/utils/theme';

const { theme } = useTheme()

</script>

<template>
    <header class="flex flex-col bg-background-dark-static">
	<div class="flex items-center h-20 px-8 gap-12">
	    <img :src="logoSvg" alt="Logo" class="w-50 h-7.5" />
	    <nav class="hidden md:flex flex-row gap-12 ">
		<RouterLink class="text-xl text-text-dark-static" :to="routeNames.releaseNotes">Release Notes</RouterLink>
		<RouterLink class="text-xl text-text-dark-static" :to="routeNames.changeNotes">Change Notes</RouterLink>
	    </nav>
	    <DropdownMenu >
		<DropdownMenuTrigger class="ml-auto">
		    <Avatar class=" w-10 h-10 items-center justify-center">
			<p class="text-sm font-bold text-text-light-static">LL</p>
		    </Avatar>
		</DropdownMenuTrigger>
		<DropdownMenuContent >
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
	<div class="md:hidden">
	    <Separator/>
	    <nav class="flex items-center justify-between px-12 h-12">
		<RouterLink class="text-lg text-text-dark-static" :to="routeNames.releaseNotes">Release Notes</RouterLink>
		<RouterLink class="text-lg text-text-dark-static" :to="routeNames.changeNotes">Change Notes</RouterLink>
	    </nav>
	    <Separator/>
	</div>
    </header>
</template>
