import axios from "axios";
import keycloak from "../keycloak";
import { config } from "@/constants";

const api = axios.create({
  baseURL: `${config.API_URL}`,
});

api.interceptors.request.use(async (config) => {
  await keycloak.updateToken(30);

  config.headers.Authorization = `Bearer ${keycloak.token}`;
  return config;
});

export default api;