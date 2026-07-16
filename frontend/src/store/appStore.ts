import type { CurrentUser } from '@/lib/api';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface AppState {
  user: CurrentUser | null | undefined;
  setUser: (value: CurrentUser | null) => void;
  removeUser: () => void;
  updateUser: (value: Partial<CurrentUser>) => void;
}

export const useAppStore = create<AppState>()(
  immer((set) => ({
    user: undefined,
    setUser: (newUser) => {
      set({ user: newUser });
    },
    removeUser: () => set({ user: null }),
    updateUser: (data: Partial<CurrentUser>) =>
      set((state) => {
        if (!state.user) return;
        Object.assign(state.user, data);
      }),
  })),
);
