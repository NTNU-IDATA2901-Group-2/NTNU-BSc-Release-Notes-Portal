import { config } from '@/utils/constants';

export function openJiraTicket(reference: string) {
  window.open(`${config.JIRA_BASE_URL}${reference}`, '_blank')?.focus();
}
