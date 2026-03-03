import type { OnApiCallFinished, PersistReleaseNoteDTO, ReleaseNote } from "@/utils/types"
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";

/**
 * Creates a new release note.
 * 
 * @param selectedItems An array of selected change note IDs to be included in the new release note.
 * @returns The ID of the newly created release note.
 * @throws An error if the API request to create the release note fails.
 */
export const createReleaseNote = async (selectedItems: number[]) => {
  const response = await api.post(`releasenotes`, { changeNoteIds: selectedItems })
  return response.data as number;
}

/**
 * Retrieves a release note by its ID.
 * 
 * @param id The ID of the release note to retrieve.
 * @throws An error if the API request to retrieve the release note fails or if the provided ID is invalid.
 * @returns A promise that resolves to the release note data retrieved from the API.
 */
export const useGetReleaseNote = (id: string) => useQuery<ReleaseNote>({
  queryKey: ['releaseNote', id],
  queryFn: () => getReleaseNote(id),
});

/**
 * Retrieves a release note by its ID from the API.
 * 
 * @param id The ID of the release note to retrieve.
 * @throws An error if the API request to retrieve the release note fails or if the provided ID is invalid.
 * @returns A promise that resolves to the release note data retrieved from the API.
 */
const getReleaseNote = async (id: string): Promise<ReleaseNote> => {
  const parsedId = Number.parseInt(id, 10)
  if (Number.isNaN(parsedId)) {
    throw new TypeError("Invalid release note ID")
  }

  const response = await api.get(`releasenotes/${id}`)
  return response.data as ReleaseNote;
}

/**
 * Retrieves a list of all release notes.
 * 
 * @throws An error if the API request to retrieve the release notes fails.
 * @returns A promise that resolves to an array of release note data retrieved from the API.
 */
export const useGetReleaseNotes = () => useQuery<ReleaseNote[]>({
  queryKey: ['releaseNotes'],
  queryFn: () => getReleaseNotes(),
});

/**
 * Retrieves a list of all release notes from the API.
 * 
 * @throws An error if the API request to retrieve the release notes fails.
 * @returns A promise that resolves to an array of release note data retrieved from the API.
 */
export const getReleaseNotes = async (params?: Record<string, string>): Promise<ReleaseNote[]> => {
  const response = await api.get(`releasenotes`, { params: params})
  return response.data as ReleaseNote[];
}

/**
 * Archives a release note by its ID. Returns a promise that resolves to true if the release note was successfully archived.
 * 
 * @param id The ID of the release note to be archived.
 * @param onFinished An object containing callback functions to be called when the API call is finished, successful, or encounters an error.
 * @returns A promise that resolves to true if the release note was successfully archived.
 * @throws An error if the API request to archive the release note fails.
 */
export const useArchiveReleaseNote = (id: string, onFinished: OnApiCallFinished) => {
  const queryClient = useQueryClient()

  return useMutation<number, void>({
  mutationFn: () => archiveReleaseNote(id),
  onSettled: () => onFinished.onSettled?.(),
  onSuccess: (data) => {
    console.log("Release note archived with ID:", data);
    queryClient.invalidateQueries({ queryKey: ['releaseNotes'] });
    onFinished.onSuccess();
  },
  onError: () => {
    console.error("Failed to archive release note with ID:", id);
    onFinished.onError();
  },

})
}

/**
 * Updates a release note by its ID with the provided release note data. Returns a promise that resolves when the release note is successfully updated.
 * @param id The ID of the release note to be updated.
 * @param releaseNoteData The updated release note data to be sent in the API request.
 */
export const updateReleaseNote = async (id: string, releaseNoteData: PersistReleaseNoteDTO): Promise<void> => {
  await api.put(`releasenotes/${id}`, releaseNoteData);
}

/**
 * Archives a release note by its ID. Returns the ID of the archived release note if successful.
 * 
 * @param id The ID of the release note to be archived.
 * @returns The ID of the archived release note if the operation was successful.
 * @throws An error if the API request to archive the release note fails or if the provided ID is invalid.
 */
const archiveReleaseNote = async (id: string): Promise<number> => {
  const parsedId = Number.parseInt(id, 10)
  if (Number.isNaN(parsedId)) {
    throw new TypeError("Invalid release note ID")
  }
  
  const response = await api.patch(`releasenotes/${id}/archive`)
  return response.data as number;
}

/**
 * Publishes or unpublishes a release note by its ID.
 * @param id The ID of the release note to be published or unpublished.
 * @param publish Whether to publish (true) or unpublish (false) the release note.
 * @return A promise that resolves when the release note is successfully published or unpublished.
 * @throws An error if the API request to publish or unpublish the release note fails or if the provided ID is invalid.
 */
export const publishReleaseNote = async (id: string, publish: boolean): Promise<void> => {
  await api.patch(`releasenotes/${id}/publish?publish=${publish}`);
}