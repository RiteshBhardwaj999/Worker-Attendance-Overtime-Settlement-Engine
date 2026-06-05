import api from './axiosInstance';

export const getTasks = (projectId) => api.get(`/projects/${projectId}/tasks`);
export const createTask = (projectId, data) => api.post(`/projects/${projectId}/tasks`, data);
export const updateTask = (projectId, taskId, data) => api.patch(`/projects/${projectId}/tasks/${taskId}`, data);
export const updateTaskStatus = (projectId, taskId, data) => api.patch(`/projects/${projectId}/tasks/${taskId}/status`, data);
export const deleteTask = (projectId, taskId) => api.delete(`/projects/${projectId}/tasks/${taskId}`);
