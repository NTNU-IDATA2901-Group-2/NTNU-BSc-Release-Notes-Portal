import eslint from '@eslint/js';
import eslintConfigPrettier from 'eslint-config-prettier';
import eslintPluginVue from 'eslint-plugin-vue';
import globals from 'globals';
import typescriptEslint from 'typescript-eslint';

export default [
  { ignores: ['*.d.ts', '**/coverage', '**/dist', '**/components/ui'] },
  eslint.configs.recommended,
  ...typescriptEslint.configs.recommended,
  ...eslintPluginVue.configs['flat/strongly-recommended-error'],
  {
    files: ['**/*.{ts,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: globals.browser,
      parserOptions: {
        parser: typescriptEslint.parser,
      },
    },
    rules: {
      'vue/no-unused-vars': 'error'
    },
  },
  eslintConfigPrettier
];