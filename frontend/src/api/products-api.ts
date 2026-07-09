import type { OnMutationApiCallFinished, PersistTagDTO, Product } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";

/**
 * Custom hook for retrieving a list of all products.
 *
 * @returns An array of product data retrieved from the API.
 */
export const useGetProducts = () => useQuery({
  queryKey: ['products'],
  queryFn: () => getProducts(),
});

/**
 * Retrieves a list of all products from the API.
 *
 * @throws An error if the API request to retrieve the products fails.
 * @returns A promise that resolves to an array of product data retrieved from the API.
 */
const getProducts = async () => {
  const response = await api.get(`products`)
  return response.data as Product[];
}

/**
 * Creates a new product with the provided dto.
 *
 * @param dto An object containing the name of the product to be created.
 * @returns the ID of the newly created product when the API request is successful.
 * @throws An error if the API request to create the product fails.
 */
const createProduct = async (dto: PersistTagDTO) => {
    const response = await api.post(`products`, dto);
    return response.data;
}

/**
 * Persists a new product using the provided data and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the creation of a new product.
 */
export const usePersistProduct = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (dto: PersistTagDTO) => createProduct(dto),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to create product");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Updates the name of a product by its ID.
 *
 * @param id The ID of the product to update.
 * @param dto An object containing the new name of the product.
 * @returns the data from the API response when the update is successful.
 * @throws An error if the API request to update the product fails.
 */
const updateProduct = async (id: number, dto: PersistTagDTO) => {
    const response = await api.put(`products/${id}`, dto);
    return response.data;
}

/**
 * Updates a product and handles the API call lifecycle.
 * Change and release note queries are invalidated as well since they embed the product name.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the product update.
 */
export const useUpdateProduct = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, name }: { id: number, name: string }) => updateProduct(id, { name }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] });
            queryClient.invalidateQueries({ queryKey: ['changeNote'] });
            queryClient.invalidateQueries({ queryKey: ['changeNotes'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNote'] });
            queryClient.invalidateQueries({ queryKey: ['releaseNotes'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to update product");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Deletes a product by its ID.
 *
 * @param id The ID of the product to delete.
 * @returns the data from the API response when the deletion is successful.
 * @throws An error if the API request to delete the product fails.
 */
const deleteProduct = async (id: number) => {
    const response = await api.delete(`products/${id}`);
    return response.data;
}

/**
 * Deletes a product by its ID and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the deletion of a product.
 */
export const useDeleteProduct = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteProduct(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] });
            onFinished.onSuccess();
        },
        onError: (error) => {
            console.error("Failed to delete product");
            onFinished.onError(error);
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}
