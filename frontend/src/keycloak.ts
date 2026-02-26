import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:8081",
  realm: "dev",
  clientId: "public",
});

export default keycloak;