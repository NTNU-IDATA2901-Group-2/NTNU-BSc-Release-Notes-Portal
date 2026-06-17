import { i18n } from '@/utils/i18n';

export function getLocaleDateString(dateString: string ): string {
  const locale = i18n.global.locale
  const date = new Date(dateString);

  switch (locale) {
    case 'en':
      return date.toLocaleDateString('en-UK');
    case 'fr':
      return date.toLocaleDateString('fr-FR');
    default:
      return date.toLocaleDateString('nb-NO'); // Default to Norwegian format
  }
}