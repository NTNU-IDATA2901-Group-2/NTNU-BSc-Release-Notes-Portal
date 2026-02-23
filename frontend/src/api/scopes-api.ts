import { config } from "@/constants";
import type { Scope } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export const useScopes = () => useQuery({
  queryKey: ['scopes'],
  queryFn: () => getScopes(),
});

const getScopes = async () => {
  const response = await axios.get(`${config.API_URL}scopes`)
  return response.data as Scope[];
}