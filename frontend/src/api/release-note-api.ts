import { config } from "@/constants";
import type { ReleaseNote } from "@/types"
import { useQuery } from "@tanstack/vue-query";

import axios from 'axios';

export const createReleaseNote = async (): Promise<number> => {
  return 1
}

export const useReleaseNote = (id: string) => useQuery<ReleaseNote>({
  queryKey: ['releaseNote', id],
  queryFn: () => getReleaseNote(id),
});

const getReleaseNote = async (id: string): Promise<ReleaseNote> => {
  const parsedId = Number.parseInt(id, 10)
  if (Number.isNaN(parsedId)) {
    throw new TypeError("Invalid release note ID")
  }

  const response = await axios.get(`${config.API_URL}releasenotes/${id}`)
  return response.data as ReleaseNote;
}

export const useReleaseNotes = () => useQuery<ReleaseNote[]>({
  queryKey: ['releaseNotes'],
  queryFn: () => getReleaseNotes(),
});

const getReleaseNotes = async (): Promise<ReleaseNote[]> => {
  const response = await axios.get(`${config.API_URL}releasenotes`)
  return response.data as ReleaseNote[];
}