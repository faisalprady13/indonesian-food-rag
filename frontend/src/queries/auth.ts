import { http } from '@/queries/http.ts';
import { setToken, clearToken } from '@/lib/token.ts';

export interface CurrentUser {
  id: number;
  username: string;
  fullname: string;
  email: string;
  imageUrl: string | null;
  provider: string | null;
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
  fullname: string;
}

export async function login(username: string, password: string): Promise<void> {
  const { data: token } = await http.post<string>('/api/auth/login', { username, password });
  if (token === 'fail') {
    throw new Error('Invalid username or password');
  }
  setToken(token);
}

export async function register(payload: RegisterPayload): Promise<CurrentUser> {
  const { data } = await http.post<CurrentUser>('/api/auth/register', payload);
  return data;
}

export async function getMe(): Promise<CurrentUser> {
  const { data } = await http.get<CurrentUser>('/api/auth');
  return data;
}

export async function updateUserApi(user: CurrentUser, file?: File): Promise<CurrentUser> {
  const formData = new FormData();
  if (file) {
    formData.append('file', file);
  }
  formData.append('fullname', user.fullname);

  const { data } = await http.put<CurrentUser>(`/api/user/${user.id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return data;
}

export async function deleteUserApi(id: number, username: string): Promise<void> {
  await http.delete(`/api/user/${id}/${username}`);
}

export function logout(): void {
  clearToken();
}
