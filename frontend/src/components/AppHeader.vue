<script setup lang="ts">

import { routeNames, router } from '../utils/router';
import logoSvg from '../assets/solwr_logo.svg';
import Avatar from './ui/avatar/Avatar.vue';
import Separator from './ui/separator/Separator.vue';
import { GitBranch, SunMoon, LogOut } from "lucide-vue-next"
import { useTheme } from '@/utils/theme';
import keycloak, { isAuthenticated, jwtTokenDecoded, isAdmin } from '@/utils/keycloak';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import MenubarSub from './ui/menubar/MenubarSub.vue';
import MenubarSubTrigger from './ui/menubar/MenubarSubTrigger.vue';
import MenubarSubContent from './ui/menubar/MenubarSubContent.vue';
import MenubarItem from './ui/menubar/MenubarItem.vue';
import Menubar from './ui/menubar/Menubar.vue';
import MenubarMenu from './ui/menubar/MenubarMenu.vue';
import MenubarTrigger from './ui/menubar/MenubarTrigger.vue';
import MenubarContent from './ui/menubar/MenubarContent.vue';
import MenubarSeparator from './ui/menubar/MenubarSeparator.vue';
import { i18n } from '@/utils/i18n';

const { theme } = useTheme()
const { t } = useI18n()

const handleLogOut = () => {
	keycloak.logout({
		redirectUri: `${globalThis.location.origin}${routeNames.signIn}`
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

const handleLocalChange = (locale : "en" | "no" | "fr") => {
	i18n.global.locale = locale;
  localStorage.setItem('locale', locale);
}

const handleLogoClick = () => {
	if (isAuthenticated.value) {
		router.push(routeNames.releaseNotes);
	}
}
</script>

<template>
    <header class="flex flex-col bg-background-dark-static">
	<div class="flex items-center h-20 px-8 gap-12">
	    <img :src="logoSvg" alt="Logo" class="w-50 h-7.5 cursor-pointer" @click="handleLogoClick"/>
	    <nav class="hidden md:flex flex-row gap-12">
		<RouterLink v-if="isAuthenticated" class="text-md text-text-dark-static hover:underline" :to="routeNames.releaseNotes">{{ t('header.releaseNotesLink') }}</RouterLink>
		<RouterLink v-if="isAuthenticated" class="text-md text-text-dark-static hover:underline" :to="routeNames.changeNotes">{{ t('header.changeNotesLink') }}</RouterLink>
	    </nav>
		<Menubar v-if="isAuthenticated" class="ml-auto">
			<MenubarMenu>
				<MenubarTrigger class="cursor-pointer">
					<Avatar class="w-10 h-10 items-center justify-center rounded-full">
						<p class="text-sm font-bold text-text-light-static">{{ firstLetters }}</p>
		    		</Avatar>
				</MenubarTrigger>
				<MenubarContent>
					<MenubarItem @click="theme = theme === 'dark' ? 'light' : 'dark'">
						<div class="w-full flex gap-2">
			    			<p class="ml-auto">{{ t('header.toggleTheme') }}</p>
			    			<SunMoon class="text-text-primary"/>
						</div>
					</MenubarItem>
					<MenubarSeparator/>
					<MenubarSub>
						<MenubarSubTrigger>
							{{ t('header.language') }}
						</MenubarSubTrigger>
						<MenubarSubContent>
							<MenubarItem @click="handleLocalChange('en')">
								English
							</MenubarItem>
							<MenubarItem @click="handleLocalChange('no')">
								Norsk
							</MenubarItem>
							<MenubarItem @click="handleLocalChange('fr')">
								Français
							</MenubarItem>
						</MenubarSubContent>
					</MenubarSub>
          <template v-if="isAdmin">
            <MenubarSeparator/>
            <MenubarItem>
              <div class="w-full flex justify-end gap-2">
                <RouterLink class="text-md" :to="routeNames.gitRepositories">{{ t('header.repositories') }}</RouterLink>
                <GitBranch class="text-text-primary"/>
              </div>
            </MenubarItem>
          </template>
					<MenubarSeparator />
					<MenubarItem @click="handleLogOut">
						<div class="w-full flex gap-2">
			    			<p class="ml-auto">{{ t('header.signOut') }}</p>
			    			<LogOut class="text-text-primary"/>
						</div>
					</MenubarItem>
				</MenubarContent>
			</MenubarMenu>
		</Menubar>
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
