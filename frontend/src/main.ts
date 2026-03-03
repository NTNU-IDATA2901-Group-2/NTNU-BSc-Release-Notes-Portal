import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './utils/router'
import { VueQueryPlugin } from '@tanstack/vue-query';
import 'vue-sonner/style.css'
import keycloak from './utils/keycloak';
import { i18n } from './utils/i18n';

const app = createApp(App)
  app.use(VueQueryPlugin)
  app.use(i18n)

keycloak.init({
  onLoad: "check-sso",
  checkLoginIframe: false,
}).then((authenticated) => {
  console.log("Keycloak initialized:", authenticated);
  // Idk if this is the best way to do this, but it ensures router is not used before keycloak is initialized to prevent sync issues with auth state.
  app.use(router)
  app.mount("#app");
});