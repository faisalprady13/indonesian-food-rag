import axios from 'axios';
import { getToken } from '@/lib/token.ts';

export const http = axios.create();

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
