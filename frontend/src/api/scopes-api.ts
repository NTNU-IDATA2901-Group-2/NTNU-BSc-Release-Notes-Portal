import type { OnMutationApiCallFinished, PersistTagDTO, Scope } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";

/**
 * Custom hook for retrieving a list of all scopes.
 *
 * @returns An array of scope data retrieved from the API.
 */
export const useGetScopes = () => useQuery({
  queryKey: ['scopes'],
  queryFn: () => getScopes(),
});

/**
 * Retrieves a list of all scopes from the API.
 *
 * @throws An error if the API request to retrieve the scopes fails.
 * @returns A promise that resolves to an array of scope data retrieved from the API.
 */
const getScopes = async () => {
  const response = await api.get(`scopes`)
  return response.data as Scope[];
}

/**
 * Creates a new scope with the provided dto.
 *
 * @param dto An object containing the name of the scope to be created.
 * @returns the ID of the newly created scope when the API request is successful.
 * @throws An error if the API request to create the scope fails.
 */
const createScope = async (dto: PersistTagDTO) => {
    const response = await api.post(`scopes`, dto);
    return response.data;
}

/**
 * Persists a new scope using the provided data and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the creation of a new scope.
 */
export const usePersistScope = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (dto: PersistTagDTO) => createScope(dto),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['scopes'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to create scope");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Updates the name of a scope by its ID.
 *
 * @param id The ID of the scope to update.
 * @param dto An object containing the new name of the scope.
 * @returns the data from the API response when the update is successful.
 * @throws An error if the API request to update the scope fails.
 */
const updateScope = async (id: number, dto: PersistTagDTO) => {
    const response = await api.put(`scopes/${id}`, dto);
    return response.data;
}

/**
 * Updates a scope and handles the API call lifecycle.
 * Change and release note queries are invalidated as well since they embed the scope name.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the scope update.
 */
export const useUpdateScope = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, name }: { id: number, name: string }) => updateScope(id, { name }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['scopes'] });
            queryClient.invalidateQueries({ queryKey: ['changeNote'] });
            queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNote'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNotes'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to update scope");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Deletes a scope by its ID.
 *
 * @param id The ID of the scope to delete.
 * @returns the data from the API response when the deletion is successful.
 * @throws An error if the API request to delete the scope fails.
 */
const deleteScope = async (id: number) => {
    const response = await api.delete(`scopes/${id}`);
    return response.data;
}

/**
 * Deletes a scope by its ID and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the deletion of a scope.
 */
export const useDeleteScope = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteScope(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['scopes'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to delete scope");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}
