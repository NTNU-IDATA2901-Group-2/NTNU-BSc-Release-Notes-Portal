import type { GitRepository, OnMutationApiCallFinished, PersistGitRepositoryDTO } from "@/utils/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import api from "./api";

/**
 * Custom hook for retrieving a list of all git repositories.
 * 
 * @returns a list of all git repositories.
 */
export const useGetGitRepositories = () => useQuery({
  queryKey: ['git-repositories'],
  queryFn: () => getGitRepositories(),
});

/**
 * Retrieves a list of all git repositories from the API.
 * 
 * @returns An array of git repository data retrieved from the API.
 * @throws An error if the API request to retrieve the git repositories fails.
 * @returns A promise that resolves to an array of git repository data retrieved from the API.
 */
const getGitRepositories = async () => {
  const response = await api.get(`git-repositories`)
  return response.data as GitRepository[];
}

/**
 * Creates a new git repository with the provided dto.
 * 
 * @param dto - An object containing the name and URL of the git repository to be created.
 * @returns the data of the newly created git repository when the API request is successful.
 * @throws An error if the API request to create the git repository fails.
 */
const createGitRepository = async (dto: PersistGitRepositoryDTO) => {
  const response = await api.post(`git-repositories`, dto);
  return response.data;
}

/**
 * Persists a new git repository using the provided data and handles the API call lifecycle.
 * 
 * @param onFinished - An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the creation of a new git repository. The mutation function will return the data of the newly created git repository when it is successful.
 */
export const usePersistGitRepository = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();

    return useMutation<PersistGitRepositoryDTO, unknown, PersistGitRepositoryDTO>({
        mutationFn: ((dto: PersistGitRepositoryDTO) => createGitRepository(dto)),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['git-repositories'] });
            onFinished.onSuccess();
        },
        onError: () => {
            console.error("Failed to create git repository");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Deletes a git repository by its ID.
 * 
 * @param id The ID of the git repository to delete.
 * @returns the data from the API response when the deletion is successful.
 * @throws An error if the API request to delete the git repository fails or if the provided ID is invalid.
 */
const deleteGitRepository = async (id: number) => {
    const params = new URLSearchParams({ id: id.toString() });
    const response = await api.delete(`git-repositories`, { params });
    return response.data;
}

/**
 * Deletes a git repository by its ID and handles the API call lifecycle.
 * 
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the deletion of a git repository. The mutation function will return the data from the API response when it is successful.
 */
export const useDeleteGitRepository = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => deleteGitRepository(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['git-repositories'] });
            onFinished.onSuccess();
        },
        onError: () => {
            console.error("Failed to delete git repository");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Replaces the personal access token of a git repository.
 *
 * @param id The ID of the git repository to update.
 * @param pat The new personal access token.
 * @returns the data from the API response when the update is successful.
 * @throws An error if the API request to update the access token fails.
 */
const updateGitRepositoryPat = async (id: number, pat: string) => {
    const response = await api.patch(`git-repositories/${id}/pat`, { pat });
    return response.data;
}

/**
 * Replaces the personal access token of a git repository and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the access token update.
 */
export const useUpdateGitRepositoryPat = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, pat }: { id: number, pat: string }) => updateGitRepositoryPat(id, pat),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['git-repositories'] });
            onFinished.onSuccess();
        },
        onError: () => {
            console.error("Failed to update git repository access token");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Synchronizes all git repositories.
 *
 * @returns the data from the API response.
 * @throws An error if the API request to synchronize the git repositories fails.
 */
const syncAllRepositories = async () => {
    const response = await api.post(`git-repositories/sync`);
    return response.data;
}

/**
 * Synchronizes all git repositories and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the synchronization of all git repositories.
 */
export const useSyncAllRepositories = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => syncAllRepositories(),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['git-repositories'] });
            queryClient.invalidateQueries({ queryKey: ['changeNotes']})
            onFinished.onSuccess();
        },
        onError: () => {
            console.error("Failed to sync git repositories");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Synchronizes a git repository by its ID.
 *
 * @param id The ID of the git repository to synchronize.
 * @returns the data from the API response.
 * @throws An error if the API request to synchronize the git repository fails or if the provided ID is invalid.
 */
const syncRepository = async (id: number) => {
    const response = await api.post(`git-repositories/sync/${id}`);
    return response.data;
}

/**
 * Synchronizes a git repository by its ID and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger the synchronization of a git repository.
 */
export const useSyncRepository = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => syncRepository(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['git-repositories'] });
            queryClient.invalidateQueries({ queryKey: ['changeNotes']})
            onFinished.onSuccess();
        },
        onError: () => {
            console.error("Failed to sync git repository");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

/**
 * Commits a release note to Git by its ID.
 *
 * The release note is committed to every git repository associated with its change notes,
 * plus any additional repositories explicitly requested.
 *
 * @param id The ID of the release note to commit to Git.
 * @param additionalGitRepositoryIds Optional IDs of extra git repositories to commit to.
 * @returns the data from the API response.
 * @throws An error if the API request to commit the release note fails.
 */
const syncReleaseNoteToGit = async (id: number, additionalGitRepositoryIds?: number[]) => {
    const response = await api.post(`git-repositories/${id}/sync-to-git`, additionalGitRepositoryIds);
    return response.data;
}

/**
 * Commits a release note to Git by its ID and handles the API call lifecycle.
 *
 * @param onFinished An object containing callback functions to be called on success, error, and settled states of the API call.
 * @returns A mutation object that can be used to trigger committing a release note to Git.
 */
export const useSyncReleaseNoteToGit = (onFinished: OnMutationApiCallFinished) => {
    const queryClient = useQueryClient();
    interface SyncReleaseNoteToGitVariables {
        id: number,
        additionalGitRepositoryIds?: number[]
    }

    let id: number | undefined;
    return useMutation({
        mutationFn: (variables: SyncReleaseNoteToGitVariables) => {
            id = variables.id;
            return syncReleaseNoteToGit(variables.id, variables.additionalGitRepositoryIds);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['releaseNote', id?.toString()] });
            onFinished.onSuccess()
        },
        onError: () => {
            console.error("Failed to sync release note to git");
            onFinished.onError();
        },
        onSettled: () => onFinished.onSettled?.(),
    })
}

