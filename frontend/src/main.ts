import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './utils/router'
import { VueQueryPlugin } from '@tanstack/vue-query';
import 'vue-sonner/style.css'

const app = createApp(App)
app.use(VueQueryPlugin)
app.use(router)
app.mount('#app')
