import { useMutation } from '@tanstack/vue-query';
import api from './api';

/**
 * Resolves the linked Jira service-request key for each given issue key. Returns
 * an empty map when there are no issue keys.
 *
 * @param issueKeys the Jira issue keys (change-note references) to look up
 * @returns a map from issue key to its service-request key; issues without a
 *          linked service request are absent from the map
 */
const getJiraServiceRequestKeys = async (issueKeys: string[]): Promise<Record<string, string>> => {
  if (issueKeys.length === 0) {
    return {};
  }
  const response = await api.get('jira/service-requests', {
    params: { issueKeys: issueKeys.join(',') },
  });
  return response.data as Record<string, string>;
}

/**
 * Custom hook for resolving the Jira service-request keys for a set of issue keys.
 *
 * @returns A mutation resolving to a map from issue key to its service-request key.
 */
export const useGetJiraServiceRequestKeys = () =>
  useMutation<Record<string, string>, unknown, string[]>({
    mutationFn: (issueKeys) => getJiraServiceRequestKeys(issueKeys),
  });
