import type { ChangeNote, OnMutationApiCallFinished, PersistChangeNoteDTO } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";
import { unref, type MaybeRef, type Ref } from "vue";


/**
 * Publishes or unpublishes a change note using a provided ID and a boolean indicating whether to publish or unpublish. Returns true if the operation was successful, and false otherwise.
 * 
 * @param changeNoteId The ID of the change note to publish or unpublish.
 * @param publish A boolean indicating whether to publish (true) or unpublish (false) the change note.
 * @returns A promise that resolves to true if the operation was successful.
 * @throws An error if the API request fails.
 */
const publishChangeNote = async (changeNoteId: number, publish: boolean): Promise<boolean> => {
  const params = new URLSearchParams({ publish: publish.toString() });

  await api.patch(`changenotes/${changeNoteId}/publish?${params.toString()}`);
  return true
}

/**
 * Custom hook for publishing or unpublishing a change note.
 * 
 * @param id the ID of the change note to be published or unpublished
 * @param publish a boolean indicating whether to publish (true) or unpublish (false) the change note
 * @param onFinished an object containing callback functions to handle the success, error, and settled states of the mutation
 * @returns a mutation object that can be used to trigger the publishing or unpublishing process and manage its state
 */
export const usePublishChangeNote = (id: number, publish: boolean, onFinished: OnMutationApiCallFinished) => {
  const queryClient = useQueryClient();
  return useMutation<boolean, unknown, boolean>({
    mutationFn: (publish: boolean) => publishChangeNote(id, publish),
    onSettled: () => onFinished.onSettled?.(),
    onSuccess: async () => {
      queryClient.invalidateQueries({ queryKey: ['changeNote'] })
      onFinished.onSuccess();
    },
    onError: () => {
      console.error(`Failed to ${publish ? 'publish' : 'unpublish'} change note with ID:`, id);
      onFinished.onError();
    },
    
  })
}

const publishChangeNotes = async (ids: number[], publish: boolean): Promise<boolean> => {
    const params = new URLSearchParams({ publish: publish.toString() });
    await Promise.all(ids.map(id => api.patch(`changenotes/${id}/publish?${params.toString()}`)));
    return true;
}

/**
 * Custom hook for publishing or unpublishing multiple change notes.
 * 
 * @param ids an array of IDs of the change notes to be published or unpublished
 * @param publish a boolean indicating whether to publish (true) or unpublish (false) the change notes
 * @param onFinished an object containing callback functions to handle the success, error, and settled states of the mutation
 * @returns a mutation object that can be used to trigger the publishing or unpublishing process and manage its state
 */
export const usePublishChangeNotes = (ids: number[], publish: boolean, onFinished: OnMutationApiCallFinished) => {
  const queryClient = useQueryClient();
  interface MutationVariables {
    ids: number[];
    publish: boolean;
  }

  return useMutation<boolean, unknown, MutationVariables>({
    mutationFn: (variables: MutationVariables) => publishChangeNotes(variables.ids, variables.publish),
    onSettled: () => onFinished.onSettled?.(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
      onFinished.onSuccess();
    },
    onError: () => {
      console.error(`Failed to ${publish ? 'publish' : 'unpublish'} change notes with IDs:`, ids);
      onFinished.onError();
    },
    
  })
}

/**
 * Custom hook for creating a new change note.
 * 
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @returns a mutation object that can be used to trigger the creation process and manage its state.
 */
export const useCreateChangeNote = (onFinished: OnMutationApiCallFinished) => {

  const queryClient = useQueryClient();
  return useMutation<number>({
  mutationFn: () => createChangeNote(),
  onSuccess: (data) => {
    onFinished.onSuccess(data.toString());
    queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
  },
  onError: () => {
    console.error("Failed to create change note");
    onFinished.onError();
  },
  onSettled: () => onFinished.onSettled?.(),
})
}

/**
 * Creates a new change note. The function returns the ID of the newly created change note.
 * 
 * @returns The ID of the newly created change note.
 * @throws An error if the API request to create the change note fails.
 */
const createChangeNote = async (): Promise<number> => {
  const response = await api.post(`changenotes`);
  return response.data as number;
}

/**
 * Archives a change note. Returns true if successful.
 * 
 * @param changeNoteId The ID of the change note to be archived.
 * @returns A promise that resolves to true if the change note was successfully archived.
 * @throws An error if the API request to archive the change note fails.
 */
const archiveChangeNote = async (changeNoteId: number) => {
  await api.patch(`changenotes/${changeNoteId}/archive`);
  return true;
}

/**
 * Custom hook for archiving a change note.
 * 
 * @param id The ID of the change note to be archived.
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @returns a mutation object that can be used to trigger the archiving process and manage its state.
 */
export const useArchiveChangeNote = (id: number, onFinished: OnMutationApiCallFinished) => {
  const queryClient = useQueryClient();
  return useMutation<boolean>({
    mutationFn: () => archiveChangeNote(id),
    onSettled: () => onFinished.onSettled?.(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
      onFinished.onSuccess();
    },
    onError: () => {
      console.error("Failed to archive change note with ID:", id);
      onFinished.onError();
    }
})}



/**
 * Updates a change note by its id. Returns a promise that resolves when the change note is successfully updated.
 * 
 * @param changeNoteId The ID of the change note to be updated.
 * @param changeNoteData The updated change note data.
 * @return A promise that resolves when the change note is successfully updated.
 * @throws An error if the API request to update the change note fails.
 */
const updateChangeNote = async (changeNoteId: number | undefined, changeNoteData: PersistChangeNoteDTO | undefined): Promise<void> => {
  if (changeNoteId === undefined) {
    console.error("Change note ID is undefined. Cannot update change note.");
  } else if (changeNoteData === undefined) {
    console.error("Change note data is undefined. Cannot update change note with ID:", changeNoteId);
  } else {
    await api.put(`changenotes/${changeNoteId}`, changeNoteData);
  }
  
}

/**
 * Custom hook for updating a change note by its ID.
 * 
 * @param onFinished An object containing callback functions to handle the success, error, and settled states of the mutation.
 * @returns a mutation object that can be used to trigger the update process and manage its state.
 */
export const useUpdateChangeNote = (onFinished: OnMutationApiCallFinished) => {
  const queryClient = useQueryClient();
  interface MutationVariables {
    id: number;
    dto: PersistChangeNoteDTO;
  }

  let updateId: number | undefined;

  return useMutation<void, unknown, MutationVariables>({
    mutationFn: ({ id, dto }: MutationVariables) => {
      updateId = id;
      return updateChangeNote(id, dto)
    },
    onSettled: () => onFinished.onSettled?.(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['changeNote', `${updateId}`] });
      onFinished.onSuccess();
    },
    onError: () => {
      console.error("Failed to update change note with ID:", updateId);
      onFinished.onError();
    }
})}

/**
 * Custom hook for retrieving a change note by its ID.
 * 
 * @param id The ID of the change note to be retrieved.
 * @returns The change note data corresponding to the provided ID.
 */
export const useGetChangeNote = (id: string) => useQuery<ChangeNote>({
  queryKey: ['changeNote', id],
  queryFn: () => getChangeNote(id),
});

/**
 * Retrieves a change note by its ID.
 * 
 * @param id The ID of the change note to be retrieved.
 * @returns The change note data corresponding to the provided ID.
 * @throws An error if the API request to retrieve the change note fails.
 */
const getChangeNote = async (id: string): Promise<ChangeNote> => {
  const response = await api.get(`changenotes/${id}`)
  return response.data as ChangeNote;
}

/**
 * Custom hook for retrieving a list of change notes. Supports optional URL search params for filtering.
 * 
 * @param params Optional URL search parameters to filter the change notes.
 * @returns An array of change note data that matches the provided search parameters.
 */
export const useGetChangeNotes = (searchParams?: Ref<Record<string, string>> | URLSearchParams) => useQuery<ChangeNote[]>({  
  queryKey: ['changeNotes', searchParams],
  queryFn: () => getChangeNotes(new URLSearchParams(searchParams instanceof URLSearchParams ? searchParams : searchParams?.value)),
});


/**
 * Retrieves a list of change notes. Supports optional URL search params for filtering.
 * 
 * @param params Optional URL search parameters to filter the change notes.
 * @returns An array of change note data that matches the provided search parameters.
 * @throws An error if the API request to retrieve the change notes fails.
 */
const getChangeNotes = async (params?: URLSearchParams) => {
  const response = await api.get(`changenotes`, { params });
  return response.data as ChangeNote[];
}

/**
 * Checks if a list of change notes has any associated Git commits.
 * 
 * @param changeNoteIds An array of IDs of the change notes to check.
 * @returns A promise resolving to a boolean indicating whether the change notes have commits.
 * @throws An error if the API request to check for commits fails.
 */
const getHasCommits = async (changeNoteIds: number[]): Promise<boolean> => {
  if (changeNoteIds.length === 0) {
    return false; // no need to send request
  }
  const response = await api.get(`changenotes/has-commits/${changeNoteIds.join(',')}`);
  return response.data as boolean;
}

/**
 * Custom hook for checking if a list of change notes has any associated Git commits.
 * 
 * @param changeNoteIds An array of IDs of the change notes to check.
 * @returns A boolean indicating whether the change notes have commits.
 */
export const useGetHasCommits = (changeNoteIds: MaybeRef<number[]>) => useQuery<boolean>({
  queryKey: ['changeNote', changeNoteIds, 'hasCommits'],
  queryFn: () => getHasCommits(unref(changeNoteIds)),
})