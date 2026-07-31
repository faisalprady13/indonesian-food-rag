import type {AppTheme} from '@/types/UserSetting.ts';
import {THEME_STORAGE_KEY} from '@/constants/theme.ts';

export function applyTheme(theme: AppTheme): void {
    document.documentElement.classList.toggle('dark', theme === 'dark');
    localStorage.setItem(THEME_STORAGE_KEY, theme);
}

export function getStoredTheme(): AppTheme {
    return localStorage.getItem(THEME_STORAGE_KEY) === 'light' ? 'light' : 'dark';
}
