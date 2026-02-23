import { config } from "@/constants";
import type { Customer } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export const useCustomers = () => useQuery({
  queryKey: ['customers'],
  queryFn: () => getCustomers(),
});

const getCustomers = async () => {
  const response = await axios.get(`${config.API_URL}customers`)
  return response.data as Customer[];
}