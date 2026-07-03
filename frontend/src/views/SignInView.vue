<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { login } from '@/utils/auth';
import { LogIn } from 'lucide-vue-next';
import { useI18n } from 'vue-i18n';

const handleSignIn = () => {
  login().catch(err => {
    console.error('Sign-in error:', err);
  });
}

const { t } = useI18n();
</script>


<template>
    <div class="flex flex-col min-h-full justify-center items-center relative">
        <div class="flex flex-col md:flex-row items-center gap-8">
            <img class="w-60 md:w-80 lg:w-100" src="@/assets/welcome_drawing.svg" alt="Welcome Drawing"/>
            <div class="w-60 md:w-80 lg:w-100 flex flex-col gap-4 items-center">
                <h1 class="text-2xl">{{ t('signIn.pleaseSignIn') }}</h1>
                <!--
                <Input placeholder="Email" class="w-64"/>
                <Input placeholder="Password" type="password" class="w-64"/>
                -->
                <Button variant="outline" @click="handleSignIn">{{ t('button.signIn') }}<LogIn class="w-4 h-4 ml-2"/></Button>
            </div>
        </div>
    </div>
</template>

