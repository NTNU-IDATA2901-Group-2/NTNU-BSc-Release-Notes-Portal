import { createI18n } from 'vue-i18n';
import en from './languages/en.json';
import no from './languages/no.json';
import fr from './languages/fr.json';

const detectUserLanguage = () => {
  const storedLocale = localStorage.getItem('locale');
    if (storedLocale) {
      return storedLocale;
    }

    const userLang = navigator.language
    if (userLang.startsWith('no') || userLang.startsWith('nb') || userLang.startsWith('nn')) {
        return 'nb-NO';
    }
    if (userLang.startsWith('fr')) {
        return 'fr-FR';
    }
    return 'en-GB';
}

export const i18n = createI18n({
    locale: detectUserLanguage(),
    fallbackLocale: 'en-GB',
    messages: {
        'en-GB': en,
        'nb-NO': no,
        'fr-FR': fr,
    }
});