export type AppTheme = 'dark' | 'light';

export interface UserSetting {
  appTheme: AppTheme;
}

export interface UserSettingRequest {
  appTheme: AppTheme;
  apiKey?: string;
}

export interface UserApiKeyStatus {
  isKeyAvailable: boolean;
}
