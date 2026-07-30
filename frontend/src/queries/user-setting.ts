import { http } from '@/queries/http.ts';
import type { UserSetting, UserSettingRequest } from '@/types/UserSetting.ts';

export async function getUserSetting(signal?: AbortSignal): Promise<UserSetting> {
  const { data } = await http.get<UserSetting>('/api/user-setting', { signal });
  return data;
}

export async function updateUserSetting(payload: UserSettingRequest): Promise<UserSetting> {
  const { data } = await http.put<UserSetting>('/api/user-setting/', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}
