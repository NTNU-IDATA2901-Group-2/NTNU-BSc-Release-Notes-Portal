import { config } from "@/constants";
import type { ChangeNote } from "@/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import axios from "axios";

export const createPublishChangeNoteMutation = () => useMutation<boolean, void, number>({
    mutationFn: (changeNoteId: number) => publishChangeNote(changeNoteId),
    onSuccess: (data) => {
      console.log("Change note published with ID:", data);

      const queryClient = useQueryClient();
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
    }
});

const publishChangeNote = async (changeNoteId: number): Promise<boolean> => {
  console.log(`Publishing change note with ID: ${changeNoteId}`);
  return true;
}

export const createChangeNoteMutation = (onSuccess : (changeId : number) => void) => useMutation<number>({
    mutationFn: () => createChangeNote(),
    onSuccess: (data) => {
      console.log("Change note created with ID:", data);

      const queryClient = useQueryClient();
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
      onSuccess(data);
    }
});

const createChangeNote = async (): Promise<number> => {
  return 1
}

export const useChangeNotes = () => useQuery<ChangeNote[]>({
    queryKey: ['changeNotes'],
    queryFn: () => getChangeNotes(),
});

export const useChangeNote = (id: string ) => useQuery<ChangeNote>({
    queryKey: ['changeNote', id],
    queryFn: () => getChangeNote(id),
});

const getChangeNote = async (id: string): Promise<ChangeNote> => {
  const parsedId = Number.parseInt(id, 10)
  if (Number.isNaN(parsedId)) {
    throw new TypeError("Invalid change note ID")
  }

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
  return [
    { id: 1, name: "Product A" },
  ]
}

const getScopes = async () => {
  return [
    { id: 1, name: "Scope A" },
    { id: 2, name: "Scope B" },
  ]
}

const getFeatures = async () => {
  return [
    { id: 1, name: "Feature A" },
    { id: 2, name: "Feature B" },
    { id: 3, name: "Feature C" },
    { id: 3, name: "Feature C" }
  ]
}

const getCustomers = async () => {
  return [
    { id: 1, name: "Customer A" },
    { id: 2, name: "Customer B" },
  ]
}