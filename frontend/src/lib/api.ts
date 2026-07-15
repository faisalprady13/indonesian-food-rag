import axios from 'axios';

export interface CurrentUser {
  username: string;
  email: string;
  imageUrl: string | null;
  provider: string | null;
}

const TOKEN_KEY = 'auth_token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

const http = axios.create();

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export async function login(username: string, password: string): Promise<void> {
  const { data: token } = await http.post<string>('/api/user/login', { username, password });
  if (token === 'fail') {
    throw new Error('Invalid username or password');
  }
  setToken(token);
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

export async function register(payload: RegisterPayload): Promise<CurrentUser> {
  const { data } = await http.post<CurrentUser>('/api/user/register', payload);
  return data;
}

export async function getMe(): Promise<CurrentUser> {
  const { data } = await http.get<CurrentUser>('/api/user');
  return data;
}

export function logout(): void {
  clearToken();
}
