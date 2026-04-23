import type { Product } from "@/utils/types";
import { useQuery } from "@tanstack/vue-query";
import api from "./api";

/**
 * Custom hook for retrieving a list of all products.
 * 
 * @returns An array of product data retrieved from the API.
 */
export const useGetProducts = () => useQuery({
  queryKey: ['products'],
  queryFn: () => getProducts(),
});

/**
 * Retrieves a list of all products from the API.
 * 
 * @throws An error if the API request to retrieve the products fails.
 * @returns A promise that resolves to an array of product data retrieved from the API.
 */
const getProducts = async () => {
  const response = await api.get(`products`)
  return response.data as Product[];
}