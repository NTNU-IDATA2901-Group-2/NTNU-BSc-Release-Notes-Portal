import type { ReleaseNote } from "@/types"
import { useMutation, useQuery } from "@tanstack/vue-query";


export const createReleaseNoteMutation = (onSuccess : (releaseId : number) => void) => useMutation<number>({
    mutationFn: () => createReleaseNote(),
    onSuccess: (data) => {
      console.log("Release note created with ID:", data);
      onSuccess(data);
    }
});

export const createReleaseNote = async (): Promise<number> => {
  return 1
}

export const useReleaseNote = (id: string ) => useQuery<ReleaseNote>({
    queryKey: ['releaseNote', id],
    queryFn: () => getReleaseNote(id),
});

const getReleaseNote = async (id: string): Promise<ReleaseNote> => {
  const parsedId = Number.parseInt(id, 10)
  if (Number.isNaN(parsedId)) {
    throw new TypeError("Invalid release note ID")
  }

    const releaseNote: ReleaseNote = {
      id: 1,
      version: "1.0.0",
      description: "This is the description of release note 1.",
      changeNotes: [
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
          published: true,
          archived: false
      }    
      ],
      published: true
  } 

  return releaseNote
}