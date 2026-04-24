import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './utils/router'
import { VueQueryPlugin } from '@tanstack/vue-query';
import 'vue-sonner/style.css'
import keycloak from './utils/keycloak';
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

await keycloak.init({
  onLoad: "check-sso",
  flow: "standard",
  pkceMethod: "S256",
  checkLoginIframe: false,
})

app.use(router)
app.mount("#app");

await router.isReady()

// Remove hash from URL as it is used for authentication and should not be visible to the user
if (globalThis.location.hash) {
  const url = new URL(globalThis.location.href);
  url.hash = '';
  globalThis.history.replaceState({}, document.title, url.toString());
}