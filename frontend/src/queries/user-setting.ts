import { http } from '@/queries/http.ts';
import type { UserApiKeyStatus, UserSetting, UserSettingRequest } from '@/types/UserSetting.ts';

export async function getUserSetting(signal?: AbortSignal): Promise<UserSetting> {
  const { data } = await http.get<UserSetting>('/api/user-setting', { signal });
  return data;
}

export async function getKeyStatus(signal?: AbortSignal): Promise<UserApiKeyStatus> {
  const { data } = await http.get<UserApiKeyStatus>('/api/user-setting/key-status', { signal });
  return data;
}

export async function updateUserSetting(payload: UserSettingRequest): Promise<UserSetting> {
  const { data } = await http.put<UserSetting>('/api/user-setting/', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}
