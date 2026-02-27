import Keycloak from "keycloak-js";
import { config } from "./constants";

const keycloak = new Keycloak({
  url: config.KC_URL,
  realm: config.KC_REALM,
  clientId: config.KC_CLIENT_ID,
});

export default keycloak;