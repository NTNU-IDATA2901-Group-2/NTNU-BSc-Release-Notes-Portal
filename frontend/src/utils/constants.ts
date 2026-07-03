const prod = {
  API_URL: '/api/',
  OIDC_AUTHORITY: 'PROD_OIDC_AUTHORITY',
  OIDC_CLIENT_ID: 'PROD_OIDC_CLIENT_ID',
  OIDC_SCOPES: 'PROD_OIDC_SCOPES',
  OIDC_ROLES_CLAIM: 'roles',
  JIRA_BASE_URL: 'PROD_JIRA_BASE_URL',
}

async function getProdConfig() {
  const response = await fetch('/api/public/config');

  if (!response.ok) {
    throw new Error(`Failed to fetch config: ${response.statusText}`);
  }

  const configData = await response.json();

  prod.OIDC_AUTHORITY = configData.OIDC_ISSUER_URI;
  prod.OIDC_CLIENT_ID = configData.OIDC_CLIENT_ID;
  prod.OIDC_SCOPES = configData.OIDC_SCOPES;
  prod.OIDC_ROLES_CLAIM = configData.OIDC_ROLES_CLAIM;
  prod.JIRA_BASE_URL = configData.JIRA_BASE_URL;

  return prod;
}

const dev = {
  API_URL: 'http://localhost:8080/api/',
  OIDC_AUTHORITY: import.meta.env.VITE_OIDC_AUTHORITY ?? 'http://localhost:8081/realms/dev',
  OIDC_CLIENT_ID: import.meta.env.VITE_OIDC_CLIENT_ID ?? 'release-note',
  OIDC_SCOPES: import.meta.env.VITE_OIDC_SCOPES ?? 'openid profile',
  OIDC_ROLES_CLAIM: import.meta.env.VITE_OIDC_ROLES_CLAIM ?? 'roles',
  JIRA_BASE_URL: import.meta.env.VITE_JIRA_BASE_URL ?? '',
}


export const config = import.meta.env.DEV ? dev : await getProdConfig();
