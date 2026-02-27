import type { Feature } from "@/utils/types";
import { useQuery } from "@tanstack/vue-query";
import api from "./api";

/**
 * Retrieves a list of all features.
 * 
 * @returns An array of feature data retrieved from the API.
 * @throws An error if the API request to retrieve the features fails.
 */
export const useFeatures = () => useQuery({
  queryKey: ['features'],
  queryFn: () => getFeatures(),
});

/**
 * Retrieves a list of all features from the API.
 * 
 * @returns An array of feature data retrieved from the API.
 * @throws An error if the API request to retrieve the features fails.
 * @returns A promise that resolves to an array of feature data retrieved from the API.
 */
const getFeatures = async () => {
  const response = await api.get(`features`)
  return response.data as Feature[];
}