import { Outlet } from 'react-router-dom';
import Navbar from '@/components/navbar/Navbar.tsx';
import type { CurrentUser } from '@/lib/api.ts';

type AppLayoutProps = {
  user: CurrentUser;
  onLogout: () => void;
};

export default function AppLayout({ user, onLogout }: Readonly<AppLayoutProps>) {
  return (
    <div className="flex h-svh w-full flex-col">
      <Navbar user={user} onLogout={onLogout} />
      <main className="flex min-h-0 flex-1 flex-col overflow-y-auto p-6 md:p-10">
        <Outlet />
      </main>
    </div>
  );
}
