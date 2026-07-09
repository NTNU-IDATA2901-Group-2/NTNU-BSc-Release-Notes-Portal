import type { Customer, OnMutationApiCallFinished, PersistTagDTO } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";

/**
 * Custom hook for retrieving a list of all customers.
 *
 * @returns An array of customer data retrieved from the API.
 */
export const useGetCustomers = () => useQuery({
  queryKey: ['customers'],
  queryFn: () => getCustomers(),
});

/**
 * Retrieves a list of all customers from the API.
 *
 * @returns An array of customer data retrieved from the API.
 * @throws An error if the API request to retrieve the customers fails.
 * @returns A promise that resolves to an array of customer data retrieved from the API.
 */
const getCustomers = async () => {
  const response = await api.get(`customers`)
  return response.data as Customer[];
}

/**
 * Creates a new customer with the provided dto.
 *
 * @param dto An object containing the name of the customer to be created.
 * @returns the ID of the newly created customer when the API request is successful.
 * @throws An error if the API request to create the customer fails.
 */
const createCustomer = async (dto: PersistTagDTO) => {
    const response = await api.post(`customers`, dto);
    return response.data;
}

/**
 * Persists a new customer using the provided data and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the creation of a new customer.
 */
export const usePersistCustomer = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (dto: PersistTagDTO) => createCustomer(dto),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['customers'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to create customer");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Updates the name of a customer by its ID.
 *
 * @param id The ID of the customer to update.
 * @param dto An object containing the new name of the customer.
 * @returns the data from the API response when the update is successful.
 * @throws An error if the API request to update the customer fails.
 */
const updateCustomer = async (id: number, dto: PersistTagDTO) => {
    const response = await api.put(`customers/${id}`, dto);
    return response.data;
}

/**
 * Updates a customer and handles the API call lifecycle.
 * Change and release note queries are invalidated as well since they embed the customer name.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the customer update.
 */
export const useUpdateCustomer = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, name }: { id: number, name: string }) => updateCustomer(id, { name }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['customers'] });
            queryClient.invalidateQueries({ queryKey: ['changeNote'] });
            queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNote'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNotes'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to update customer");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Deletes a customer by its ID.
 *
 * @param id The ID of the customer to delete.
 * @returns the data from the API response when the deletion is successful.
 * @throws An error if the API request to delete the customer fails.
 */
const deleteCustomer = async (id: number) => {
    const response = await api.delete(`customers/${id}`);
    return response.data;
}

/**
 * Deletes a customer by its ID and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the deletion of a customer.
 */
export const useDeleteCustomer = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteCustomer(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['customers'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to delete customer");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}
