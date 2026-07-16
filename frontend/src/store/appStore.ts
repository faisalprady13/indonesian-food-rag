import type { CurrentUser } from '@/lib/api';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface AppState {
  user: CurrentUser | null;
  setUser: (value: CurrentUser | null) => void;
  removeUser: () => void;
  updateUser: (value: Partial<CurrentUser>) => void;
}

export const useAppStore = create<AppState>()(
  immer((set) => ({
    user: null,
    setUser: (newUser) => set({ user: newUser }),
    removeUser: () => set({ user: null }),
    updateUser: (data: Partial<CurrentUser>) =>
      set((state) => {
        Object.assign(state.user, data);
      }),
  })),
);
