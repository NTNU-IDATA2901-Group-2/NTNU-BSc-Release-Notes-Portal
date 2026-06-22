import { useQuery } from '@tanstack/vue-query';
import api from './api';

/**
 * Resolves the linked Jira service-request key for each given issue key.
 *
 * @param issueKeys the Jira issue keys (change-note references) to look up
 * @returns a map from issue key to its service-request key; issues without a
 *          linked service request are absent from the map
 */
const getJiraServiceRequestKeys = async (issueKeys: string[]) => {
  const response = await api.get('jira/service-requests', {
    params: { issueKeys: issueKeys.join(',') },
  });
  return response.data as Record<string, string>;
}

export const useGetJiraServiceRequestKeys = (issueKeys: string[]) => useQuery<Record<string, string>>({
  queryFn: () => getJiraServiceRequestKeys(issueKeys),
  queryKey: ['jira', 'service-requests', issueKeys],
  enabled: issueKeys.length > 0,
})
