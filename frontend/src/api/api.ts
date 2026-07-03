import axios from "axios";
import { getAccessToken } from "../utils/auth";
import { config } from "@/utils/constants";

const api = axios.create({
  baseURL: `${config.API_URL}`,
});

api.interceptors.request.use(async (config) => {
  const token = await getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;