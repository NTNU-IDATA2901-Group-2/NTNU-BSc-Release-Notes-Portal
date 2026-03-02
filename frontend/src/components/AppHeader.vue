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

import { SunMoon } from "lucide-vue-next"
import { LogOut } from "lucide-vue-next"
import { useTheme } from '@/utils/theme';
import keycloak, { isAuthenticated, jwtTokenDecoded } from '@/utils/keycloak';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { theme } = useTheme()
const { t } = useI18n()

const handleLogOut = () => {
	keycloak.logout({
		redirectUri: `${window.location.origin}${routeNames.signIn}`
	}).then(() => {
	}).catch(err => {
		console.error('Keycloak logout error:', err);
	});
}

const firstLetters = computed(() => {
	if (!isAuthenticated.value) return '';
	if (!jwtTokenDecoded.value) return '';

	const firstName = jwtTokenDecoded.value.family_name || '';
	const lastName = jwtTokenDecoded.value.given_name || '';
	return firstName.charAt(0).toUpperCase() + lastName.charAt(0).toUpperCase();
})

</script>

<template>
    <header class="flex flex-col bg-background-dark-static">
	<div class="flex items-center h-20 px-8 gap-12">
	    <img :src="logoSvg" alt="Logo" class="w-50 h-7.5" />
	    <nav class="hidden md:flex flex-row gap-12 ">
		<RouterLink v-if="isAuthenticated" class="text-md text-text-dark-static hover:underline" :to="routeNames.releaseNotes">{{ t('header.releaseNotesLink') }}</RouterLink>
		<RouterLink v-if="isAuthenticated" class="text-md text-text-dark-static hover:underline" :to="routeNames.changeNotes">{{ t('header.changeNotesLink') }}</RouterLink>
	    </nav>
	    <DropdownMenu v-if="isAuthenticated">
		<DropdownMenuTrigger class="ml-auto cursor-pointer">
		    <Avatar class=" w-10 h-10 items-center justify-center">
			<p class="text-sm font-bold text-text-light-static">{{ firstLetters }}</p>
		    </Avatar>
		</DropdownMenuTrigger>
		<DropdownMenuContent class="mr-8 mt-2">
		    <DropdownMenuItem @click="theme = theme === 'dark' ? 'light' : 'dark'">
			<div class="w-full flex gap-2">
			    <p class="text-text-dark-static ml-auto">{{ t('header.toggleTheme') }}</p>
			    <SunMoon class="text-text-dark-static"/>
			</div>
		    </DropdownMenuItem>
		    <DropdownMenuItem @click="handleLogOut">
			<div class="w-full flex gap-2">
			    <p class="ml-auto text-text-dark-static">{{ t('header.signOut') }}</p>
			    <LogOut class="text-text-dark-static"/>
			</div>
		    </DropdownMenuItem>
		</DropdownMenuContent>
	    </DropdownMenu>

	</div>
	<Separator/>
	<div class="md:hidden">
	    <nav class="flex items-center justify-between px-12 h-12">
		<RouterLink class="text-lg text-text-dark-static" :to="routeNames.releaseNotes">{{ t('header.releaseNotesLink') }}</RouterLink>
		<RouterLink class="text-lg text-text-dark-static" :to="routeNames.changeNotes">{{ t('header.changeNotesLink') }}</RouterLink>
	    </nav>
	    <Separator/>
	</div>
    </header>
</template>
