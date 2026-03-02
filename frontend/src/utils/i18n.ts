import { createI18n } from 'vue-i18n';
import en from './languages/en.json';
import no from './languages/no.json';

const detectUserLanguage = () => {
    const userLang = navigator.language
    if (userLang.startsWith('no') || userLang.startsWith('nb') || userLang.startsWith('nn')) {
        return 'no';
    }
    return 'en';
}

export const i18n = createI18n({
    locale: 'no',
    fallbackLocale: 'en',
    messages: {
        en,
        no
    }
});