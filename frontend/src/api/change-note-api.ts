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

export const createChangeNote = async (): Promise<number> => {
  return 1
}

export const useChangeNotes = () => useQuery<ChangeNote[]>({
    queryKey: ['changeNotes'],
    queryFn: () => getChangeNotes(),
});

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
      product: { id: 2, name: "Product B" },
      scope: { id: 2, name: "Scope B" },
      feature: { id: 2, name: "Feature B" },
      customer: { id: 2, name: "Customer B" },
      published: false,
      archived: false
    }]
}