import { config } from '@/utils/constants';

/** Builds the Jira browse URL for an issue or service-request key. */
export function jiraTicketUrl(reference: string) {
  return `${config.JIRA_BASE_URL}/browse/${reference}`;
}

export function openJiraTicket(reference: string) {
  window.open(jiraTicketUrl(reference), '_blank')?.focus();
}
