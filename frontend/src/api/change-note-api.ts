import type { ChangeNote } from "@/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";

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

  const changeNotes = await getChangeNotes();
  const changeNote = changeNotes.find(note => note.id === parsedId);
  
  if (!changeNote) {
    throw new Error(`Change note with ID ${id} not found`);
  }

  return changeNote;
}

const getChangeNotes = async () => {
  return [
    {
      id: 1,
      reference: "CHG-001",
      description: "This is the description of change note 1.",
      developerNotes: "These are the developer notes for change note 1.",
      upgradeNotes: "These are the upgrade notes for change note 1.",
      changeSource: "Internal",
      product: { id: 1, name: "Product A" },
      scope: { id: 1, name: "Scope A" },
      feature: { id: 1, name: "Feature A" },
      customer: { id: 1, name: "Customer A" },
      published: true,
      archived: false
    },
    {
      id: 2,
      reference: "CHG-002",
      description: "This is the description of change note 2.",
      developerNotes: "These are the developer notes for change note 2.",
      upgradeNotes: "These are the upgrade notes for change note 2.",
      changeSource: "External",
      product: { id: 1, name: "Product A" },
      scope: { id: 2, name: "Scope B" },
      feature: { id: 2, name: "Feature B" },
      customer: { id: 2, name: "Customer B" },
      published: false,
      archived: false
    }]
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