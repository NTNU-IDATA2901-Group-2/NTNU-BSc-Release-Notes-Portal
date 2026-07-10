import type { Feature, OnMutationApiCallFinished, PersistTagDTO } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";

/**
 * Custom hook for retrieving a list of all features.
 *
 * @returns An array of feature data retrieved from the API.
 */
export const useGetFeatures = () => useQuery({
  queryKey: ['features'],
  queryFn: () => getFeatures(),
});

/**
 * Retrieves a list of all features from the API.
 *
 * @returns An array of feature data retrieved from the API.
 * @throws An error if the API request to retrieve the features fails.
 * @returns A promise that resolves to an array of feature data retrieved from the API.
 */
const getFeatures = async () => {
  const response = await api.get(`features`)
  return response.data as Feature[];
}

/**
 * Creates a new feature with the provided dto.
 *
 * @param dto An object containing the name of the feature to be created.
 * @returns the ID of the newly created feature when the API request is successful.
 * @throws An error if the API request to create the feature fails.
 */
const createFeature = async (dto: PersistTagDTO) => {
    const response = await api.post(`features`, dto);
    return response.data;
}

/**
 * Persists a new feature using the provided data and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the creation of a new feature.
 */
export const usePersistFeature = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (dto: PersistTagDTO) => createFeature(dto),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['features'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to create feature");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Updates the name of a feature by its ID.
 *
 * @param id The ID of the feature to update.
 * @param dto An object containing the new name of the feature.
 * @returns the data from the API response when the update is successful.
 * @throws An error if the API request to update the feature fails.
 */
const updateFeature = async (id: number, dto: PersistTagDTO) => {
    const response = await api.put(`features/${id}`, dto);
    return response.data;
}

/**
 * Updates a feature and handles the API call lifecycle.
 * Change and release note queries are invalidated as well since they embed the feature name.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the feature update.
 */
export const useUpdateFeature = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, name }: { id: number, name: string }) => updateFeature(id, { name }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['features'] });
            queryClient.invalidateQueries({ queryKey: ['changeNote'] });
            queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNote'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNotes'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to update feature");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Deletes a feature by its ID.
 *
 * @param id The ID of the feature to delete.
 * @returns the data from the API response when the deletion is successful.
 * @throws An error if the API request to delete the feature fails.
 */
const deleteFeature = async (id: number) => {
    const response = await api.delete(`features/${id}`);
    return response.data;
}

/**
 * Deletes a feature by its ID and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the deletion of a feature.
 */
export const useDeleteFeature = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteFeature(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['features'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to delete feature");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}
