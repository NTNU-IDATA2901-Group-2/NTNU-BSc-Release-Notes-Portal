const prod = {
  API_URL: '/api/',
  KC_URL: 'PROD_URL',
  KC_REALM: 'PROD_REALM',
  KC_CLIENT_ID: 'PROD_CLIENT_ID',
}

async function getProdConfig() {
  const response = await fetch('/api/public/config');

  if (!response.ok) {
    throw new Error(`Failed to fetch config: ${response.statusText}`);
  }

  const configData = await response.json();

  prod.KC_CLIENT_ID = configData.KC_CLIENT_ID;
  prod.KC_REALM = configData.KC_REALM;
  prod.KC_URL = configData.KC_URL;

  return prod;
}

const dev = {
  API_URL: 'http://localhost:8080/api/',
  KC_URL: 'http://localhost:8081',
  KC_REALM: 'dev',
  KC_CLIENT_ID: 'release-note',
}


export const config = import.meta.env.DEV ? dev : await getProdConfig();