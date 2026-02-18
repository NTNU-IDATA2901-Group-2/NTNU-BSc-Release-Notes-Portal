const prod = {
    API_URL: '/'
}

const dev = {
    API_URL: 'http://localhost:8080/'
}

export const config = import.meta.env.DEV ? dev : prod
