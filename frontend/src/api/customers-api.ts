import { config } from "@/constants";
import type { Customer } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

/**
 * Retrieves a list of all customers.
 * 
 * @returns An array of customer data retrieved from the API.
 * @throws An error if the API request to retrieve the customers fails.
 */
export const useCustomers = () => useQuery({
  queryKey: ['customers'],
  queryFn: () => getCustomers(),
});

/**
 * Retrieves a list of all customers from the API. 
 * 
 * @returns An array of customer data retrieved from the API.
 * @throws An error if the API request to retrieve the customers fails.
 * @returns A promise that resolves to an array of customer data retrieved from the API.
 */
const getCustomers = async () => {
  const response = await axios.get(`${config.API_URL}customers`)
  return response.data as Customer[];
}