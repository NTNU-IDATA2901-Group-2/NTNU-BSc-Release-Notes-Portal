import Keycloak from "keycloak-js";
import { config } from "./constants";
import { computed, ref } from "vue";
import { jwtDecode } from "jwt-decode"

type DecodedJwtToken = {
  given_name?: string;
  family_name?: string;
  realm_access?: {
    roles?: string[];
  };
};

const keycloak = new Keycloak({
  url: config.KC_URL,
  realm: config.KC_REALM,
  clientId: config.KC_CLIENT_ID,
});

export const isAuthenticated = ref<boolean>(keycloak.authenticated);
export const jwtToken = ref<string | undefined>(undefined);
export const jwtTokenDecoded = ref<DecodedJwtToken | undefined>(undefined);
export const isAdmin = computed(() => {
  if (!jwtTokenDecoded.value) return false;
  const roles = jwtTokenDecoded.value.realm_access?.roles || [];
  return roles.includes("Admin");
});

keycloak.onAuthSuccess = () => {
  isAuthenticated.value = true;
  jwtToken.value = keycloak.token;
  jwtTokenDecoded.value = keycloak.token
    ? jwtDecode<DecodedJwtToken>(keycloak.token)
    : undefined;
};

keycloak.onAuthLogout = () => {
  isAuthenticated.value = false;
  jwtToken.value = undefined;
  jwtTokenDecoded.value = undefined;
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