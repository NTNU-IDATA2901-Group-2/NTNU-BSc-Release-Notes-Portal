const prod = {
    API_URL: '/'
}

const dev = {
    API_URL: 'http://localhost:8080/api/'
}

export const config = import.meta.env.DEV ? dev : prod
