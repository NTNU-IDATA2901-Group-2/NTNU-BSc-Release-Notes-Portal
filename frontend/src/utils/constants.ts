const prod = {
    API_URL: '/',
    KC_URL: 'PROD_URL',
    KC_REALM: 'PROD_REALM',
    KC_CLIENT_ID: 'PROD_CLIENT_ID',
}

const dev = {
    API_URL: 'http://localhost:8080/api/',
    KC_URL: 'http://localhost:8081',
    KC_REALM: 'dev',
    KC_CLIENT_ID: 'public',
}

export const config = import.meta.env.DEV ? dev : prod