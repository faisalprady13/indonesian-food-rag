import { Outlet } from 'react-router-dom';
import Navbar from '@/components/navbar/Navbar.tsx';
import type { CurrentUser } from '@/types/User.ts';

type AppLayoutProps = {
  user: CurrentUser;
  onLogout: () => void;
};

export default function AppLayout({ user, onLogout }: Readonly<AppLayoutProps>) {
  return (
    <div className="flex h-svh w-full flex-col">
      <Navbar user={user} onLogout={onLogout} />
      <main>
        <Outlet />
      </main>
    </div>
  );
}
