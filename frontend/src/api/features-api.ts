import { config } from "@/constants";
import type { Feature } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export const useFeatures = () => useQuery({
  queryKey: ['features'],
  queryFn: () => getFeatures(),
});

const getFeatures = async () => {
  const response = await axios.get(`${config.API_URL}features`)
  return response.data as Feature[];
}