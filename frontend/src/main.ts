import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './utils/router'
import { VueQueryPlugin } from '@tanstack/vue-query';
import 'vue-sonner/style.css'
import { initAuth } from './utils/auth';
import { i18n } from './utils/i18n';

const app = createApp(App)

app.use(VueQueryPlugin, {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        staleTime: 1000 * 60 * 30, // 30 minutes
      }
    }
  }
  },
)
app.use(i18n)

await initAuth()

app.use(router)
app.mount("#app");

await router.isReady()