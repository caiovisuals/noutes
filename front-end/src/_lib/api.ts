import axios from "axios"

export const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL,
    withCredentials: true,
})

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('noutes_token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

api.interceptors.response.use(
    (res) => res,
    (err) => {
        if (err.response?.status === 401) {
            localStorage.removeItem('noutes_token')
            window.location.href = '/login'
        }
        return Promise.reject(err)
    }
)

export const pagesApi = {
    list: (workspaceId: string) => api.get(`/workspaces/${workspaceId}/pages`),
    get: (pageId: string) => api.get(`/pages/${pageId}`),
    create: (data: { title: string; workspaceId: string; parentPageId?: string }) =>
        api.post('/pages', data),
    update: (pageId: string, data: { title?: string; isPublic?: boolean }) =>
        api.patch(`/pages/${pageId}`, data),
    delete: (pageId: string) => api.delete(`/pages/${pageId}`),
}

export const workspacesApi = {
    get: () => api.get('/workspaces/me'),
    create: (name: string) => api.post('/workspaces', { name }),
}