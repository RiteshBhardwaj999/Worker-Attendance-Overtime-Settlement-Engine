import api from './axiosInstance';

export const getProjects = () => api.get('/projects');
export const getProject = (id) => api.get(`/projects/${id}`);
export const createProject = (data) => api.post('/projects', data);
export const updateProject = (id, data) => api.patch(`/projects/${id}`, data);
export const deleteProject = (id) => api.delete(`/projects/${id}`);

export const getMembers = (projectId) => api.get(`/projects/${projectId}/members`);
export const inviteMember = (projectId, data) => api.post(`/projects/${projectId}/members`, data);
export const changeRole = (projectId, userId, data) => api.patch(`/projects/${projectId}/members/${userId}`, data);
export const removeMember = (projectId, userId) => api.delete(`/projects/${projectId}/members/${userId}`);
