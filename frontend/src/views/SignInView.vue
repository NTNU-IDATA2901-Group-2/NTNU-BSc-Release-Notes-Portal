<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import keycloak from '@/utils/keycloak';
import { LogIn } from 'lucide-vue-next';

const handleSignIn = () => {
  keycloak.login({
    redirectUri: `${window.location.origin}/`
  }).then(() => {

  }).catch(err => {
    console.error('Keycloak login error:', err);
  });
}
</script>


<template>
    <div class="flex flex-col min-h-full justify-center items-center relative">
        <div class="flex flex-col md:flex-row items-center gap-8">
            <img class="w-60 md:w-80 lg:w-100" src="@/assets/welcome_drawing.svg" alt="Welcome Drawing"/>
            <div class="w-60 md:w-80 lg:w-100 flex flex-col gap-4 items-center">
                <h1 class="text-2xl">Please sign in</h1>
                <!--
                <Input placeholder="Email" class="w-64"/>
                <Input placeholder="Password" type="password" class="w-64"/>
                -->
                <Button variant="outline" @click="handleSignIn">Sign In <LogIn class="w-4 h-4"/></Button>
            </div>
        </div>
    </div>
</template>

