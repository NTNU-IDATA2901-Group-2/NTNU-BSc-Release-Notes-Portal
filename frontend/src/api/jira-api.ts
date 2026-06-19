import { useQuery } from '@tanstack/vue-query';
import api from './api';

const getJiraServiceReqest = async (issueKey: string) => {
  const response = await api.get(`jira/service-request/${issueKey}`);
  return response.data;
}

export const useGetJiraServiceRequest = (issueKey: string) => useQuery<string>({
  queryFn: () => getJiraServiceReqest(issueKey),
  queryKey: ['jira', issueKey],
})
