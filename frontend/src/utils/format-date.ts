import { i18n } from '@/utils/i18n';

export function getLocaleDateString(dateString: string): string {
  return new Date(dateString).toLocaleDateString(i18n.global.locale);
}
