import type { ChangeNote, PersistChangeNoteDTO } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";


/**
 * Publishes or unpublishes a change note.
 * 
 * @param changeNoteId The ID of the change note to publish or unpublish.
 * @param publish A boolean indicating whether to publish (true) or unpublish (false) the change note.
 * @returns A promise that resolves to true if the operation was successful.
 * @throws An error if the API request fails.
 */
export const publishChangeNote = async (changeNoteId: number, publish: boolean): Promise<boolean> => {
  const params = new URLSearchParams({ publish: publish.toString() });

  await api.patch(`changenotes/${changeNoteId}/publish?${params.toString()}`);
  return true;
}

/**
 * Creates a change note. The functions takes a callback: onSuccess.
 * 
 * @param onSuccesss A callback function that is called with the new change note ID after the change note is successfully created.
 * @returns A mutation object that can be used to trigger the creation of a new change note. The mutation function will return the ID of the newly created change note when it is successful.
 * @throws An error if the API request to create the change note fails.
 */
export const useCreateChangeNote = (onSuccesss: (changeId: number) => void) => useMutation<number>({
  mutationFn: () => createChangeNote(),
  onSuccess: (data) => {
    onSuccesss(data);
    const queryClient = useQueryClient();
    queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
  }
});

/**
 * Creates a new change note. The function returns the ID of the newly created change note.
 * 
 * @returns The ID of the newly created change note.
 * @throws An error if the API request to create the change note fails.
 */
export const createChangeNote = async (): Promise<number> => {
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
export const archiveChangeNote = async (changeNoteId: number) => {
  await api.patch(`changenotes/${changeNoteId}/archive`);
  return true;
}

/**
 * Updates a change note by id. Returns a promise that resolves when the change note is successfully updated.
 * 
 * @param changeNoteId The ID of the change note to be updated.
 * @param changeNoteData The updated change note data.
 * @return A promise that resolves when the change note is successfully updated.
 * @throws An error if the API request to update the change note fails.
 */
export const updateChangeNote = async (changeNoteId: number, changeNoteData: PersistChangeNoteDTO): Promise<void> => {
  await api.put(`changenotes/${changeNoteId}`, changeNoteData);
}

/**
 * Retrieves a change note by its ID. Returns the change note data corresponding to the provided ID.
 * 
 * @param id The ID of the change note to be retrieved.
 * @returns The change note data corresponding to the provided ID.
 * @throws An error if the API request to retrieve the change note fails.
 */
export const useGetChangeNote = (id: string) => useQuery<ChangeNote>({
  queryKey: ['changeNote', id],
  queryFn: () => getChangeNote(id),
});

/**
 * Retrieves a change note by its ID. Returns the change note data corresponding to the provided ID.
 * 
 * @param id The ID of the change note to be retrieved.
 * @returns The change note data corresponding to the provided ID.
 * @throws An error if the API request to retrieve the change note fails.
 */
export const getChangeNote = async (id: string): Promise<ChangeNote> => {
  const response = await api.get(`changenotes/${id}`)
  return response.data as ChangeNote;
}

/**
 * Retrieves a list of all change notes. Supports optional URL search params for filtering. Returns an array of change note data that matches the provided search parameters.
 * 
 * @param params Optional URL search parameters to filter the change notes.
 * @returns An array of change note data that matches the provided search parameters.
 * @throws An error if the API request to retrieve the change notes fails.
 */
export const useGetChangeNotes = (params?: URLSearchParams) => useQuery<ChangeNote[]>({
  queryKey: ['changeNotes', params],
  queryFn: () => getChangeNotes(params),
});

/**
 * Retrieves a list of change notes. Supports optional URL search params for filtering. Returns an array of change note data that matches the provided search parameters.
 * 
 * @param params Optional URL search parameters to filter the change notes.
 * @returns An array of change note data that matches the provided search parameters.
 * @throws An error if the API request to retrieve the change notes fails.
 */
export const getChangeNotes = async (params?: URLSearchParams) => {
  const response = await api.get(`changenotes`, { params });
  return response.data as ChangeNote[];
}