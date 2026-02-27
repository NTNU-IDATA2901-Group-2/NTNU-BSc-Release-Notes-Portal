import Keycloak from "keycloak-js";
import { config } from "./constants";
import { ref } from "vue";

const keycloak = new Keycloak({
  url: config.KC_URL,
  realm: config.KC_REALM,
  clientId: config.KC_CLIENT_ID,
});

export const isAuthenticated = ref(keycloak.authenticated);

keycloak.onAuthSuccess = () => {
  isAuthenticated.value = true;
};

keycloak.onAuthLogout = () => {
  isAuthenticated.value = false;
};

keycloak.onTokenExpired = () => {
  keycloak
    .updateToken(30)
    .then((refreshed) => {
      if (refreshed) {
        console.log('Token refreshed');
        isAuthenticated.value = true;
      } else {
        console.warn('Token not refreshed, user is no longer authenticated');
        isAuthenticated.value = false;
      }
    })
    .catch(() => {
      console.error('Failed to refresh token');
      isAuthenticated.value = false;
    });
};

export default keycloak;