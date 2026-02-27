import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { router } from './utils/router'
import { VueQueryPlugin } from '@tanstack/vue-query';
import 'vue-sonner/style.css'
import keycloak from './utils/keycloak';

const app = createApp(App)
app.use(VueQueryPlugin)
app.use(router)

keycloak.init({
  onLoad: "login-required",
  checkLoginIframe: false,
}).then((authenticated) => {
  if (authenticated) {
    app.mount("#app");
  }
});

