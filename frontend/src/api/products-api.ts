import { config } from "@/constants";
import type { Product } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export const useProducts = () => useQuery({
  queryKey: ['products'],
  queryFn: () => getProducts(),
});

const getProducts = async () => {
  const response = await axios.get(`${config.API_URL}products`)
  return response.data as Product[];
}