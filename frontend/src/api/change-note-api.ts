import { config } from "@/constants";
import type { ChangeNote, Customer, Feature, PersistChangeNoteDTO, Product, Scope } from "@/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import axios from "axios";


export const publishChangeNote = async (changeNoteId: number): Promise<boolean> => {
  await axios.patch(`${config.API_URL}changenotes/${changeNoteId}/publish`);
  return true;
}

export const useCreateChangeNote = (onSuccesss : (changeId : number) => void) => useMutation<number>({
    mutationFn: () => createChangeNote(),
    onSuccess: (data) => {
      onSuccesss(data);
      const queryClient = useQueryClient();
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
    }
});

export const createChangeNote = async (): Promise<number> => {
  const response = await axios.post(`${config.API_URL}changenotes`);
  return response.data as number;
}

export const archiveChangeNote = async (changeNoteId: number) => {
  await axios.patch(`${config.API_URL}changenotes/${changeNoteId}/archive`);
  return true;
}

export const updateChangeNote = async (changeNoteId: number, changeNoteData: PersistChangeNoteDTO): Promise<void> => {
  await axios.put(`${config.API_URL}changenotes/${changeNoteId}`, changeNoteData);
}

export const useGetChangeNotes = () => useQuery<ChangeNote[]>({
    queryKey: ['changeNotes'],
    queryFn: () => getChangeNotes(),
});

export const useGetChangeNote = (id: string ) => useQuery<ChangeNote>({
    queryKey: ['changeNote', id],
    queryFn: () => getChangeNote(id),
});

export const getChangeNote = async (id: string): Promise<ChangeNote> => {
  const response = await axios.get(`${config.API_URL}changenotes/${id}`)
  return response.data as ChangeNote;
}

const getChangeNotes = async () => {
  const response = await axios.get(`${config.API_URL}changenotes`)
  return response.data as ChangeNote[];
}

export const useProducts = () => useQuery({
    queryKey: ['products'],
    queryFn: () => getProducts(),
});

export const useScopes = () => useQuery({
    queryKey: ['scopes'],
    queryFn: () => getScopes(),
});

export const useFeatures = () => useQuery({
    queryKey: ['features'],
    queryFn: () => getFeatures(),
});

export const useCustomers = () => useQuery({
    queryKey: ['customers'],
    queryFn: () => getCustomers(),
});

const getProducts = async () => {
  const response = await axios.get(`${config.API_URL}products`)
  return response.data as Product[];
}

const getScopes = async () => {
  const response = await axios.get(`${config.API_URL}scopes`)
  return response.data as Scope[];
}

const getFeatures = async () => {
  const response = await axios.get(`${config.API_URL}features`)
  return response.data as Feature[];
}

const getCustomers = async () => {
  const response = await axios.get(`${config.API_URL}customers`)
  return response.data as Customer[];
}