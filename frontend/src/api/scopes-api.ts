import type { Scope } from "@/utils/types";
import { useQuery } from "@tanstack/vue-query";
import api from "./api";

/**
 * Retrieves a list of all scopes.
 * 
 * @returns An array of scope data retrieved from the API.
 * @throws An error if the API request to retrieve the scopes fails.
 */
export const useGetScopes = () => useQuery({
  queryKey: ['scopes'],
  queryFn: () => getScopes(),
});

/**
 * Retrieves a list of all scopes from the API.
 * 
 * @returns An array of scope data retrieved from the API.
 * @throws An error if the API request to retrieve the scopes fails.
 * @returns A promise that resolves to an array of scope data retrieved from the API.
 */
const getScopes = async () => {
  const response = await api.get(`scopes`)
  return response.data as Scope[];
}