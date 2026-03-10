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
        return 'no';
    }
    if (userLang.startsWith('fr')) {
        return 'fr';
    }
    return 'en';
}

export const i18n = createI18n({
    locale: detectUserLanguage(),
    fallbackLocale: 'en',
    messages: {
        en,
        no,
        fr
    }
});