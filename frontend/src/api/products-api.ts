import { config } from "@/constants";
import type { Product } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

/**
 * Retrieves a list of all products.
 * 
 * @returns An array of product data retrieved from the API.
 * @throws An error if the API request to retrieve the products fails.
 */
export const useProducts = () => useQuery({
  queryKey: ['products'],
  queryFn: () => getProducts(),
});

/**
 * Retrieves a list of all products from the API.
 * 
 * @returns An array of product data retrieved from the API.
 * @throws An error if the API request to retrieve the products fails.
 * @returns A promise that resolves to an array of product data retrieved from the API.
 */
const getProducts = async () => {
  const response = await axios.get(`${config.API_URL}products`)
  return response.data as Product[];
}