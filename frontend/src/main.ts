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

const authenticated = await keycloak.init({
  onLoad: "check-sso",
  flow: "standard",
  pkceMethod: "S256",
  checkLoginIframe: false,
})

console.log("Keycloak initialized:", authenticated);
// Idk if this is the best way to do this, but it ensures router is not used before keycloak is initialized to prevent sync issues with auth state.
app.use(router)
app.mount("#app");

// Remove the hash from the URL as it is not needed after initialization and can cause issues with routing.
await router.isReady()

if (globalThis.location.hash) {
  const url = new URL(globalThis.location.href);
  url.hash = '';
  globalThis.history.replaceState({}, document.title, url.toString());
}