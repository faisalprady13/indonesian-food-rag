import type { AppTheme } from '@/types/UserSetting.ts';

const THEME_STORAGE_KEY = 'app_theme';

export function applyTheme(theme: AppTheme): void {
  document.documentElement.classList.toggle('dark', theme === 'dark');
  localStorage.setItem(THEME_STORAGE_KEY, theme);
}

export function getStoredTheme(): AppTheme {
  return localStorage.getItem(THEME_STORAGE_KEY) === 'light' ? 'light' : 'dark';
}
