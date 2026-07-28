export type AppTheme = 'dark' | 'light';

export interface UserSetting {
  id: number;
  appTheme: AppTheme;
}

export interface UserSettingRequest {
  appTheme: AppTheme;
  apiKey?: string;
}
