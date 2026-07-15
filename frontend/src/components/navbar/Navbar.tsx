import { HugeiconsIcon } from '@hugeicons/react';
import { HomeIcon, UserIcon, LogoutIcon } from '@hugeicons/core-free-icons';
import { NavLink } from 'react-router-dom';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar.tsx';
import { Button } from '@/components/ui/button.tsx';
import type { CurrentUser } from '@/lib/api.ts';
import Logo from '@/assets/logo.webp';

type NavbarProps = {
  user: CurrentUser;
  onLogout: () => void;
};

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-sm font-medium transition-colors ${
    isActive
      ? 'bg-muted text-foreground'
      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
  }`;

export default function Navbar({ user, onLogout }: Readonly<NavbarProps>) {
  return (
    <header className="flex items-center justify-between border-b border-border px-4 py-3 sm:px-6">
      <div className="flex items-center gap-6">
        <img src={Logo} alt="Logo" className="h-8 w-auto" />
        <nav className="flex items-center gap-1">
          <NavLink to="/" end className={navLinkClass}>
            <HugeiconsIcon icon={HomeIcon} size={18} />
            Home
          </NavLink>
          <NavLink to="/profile" className={navLinkClass}>
            <HugeiconsIcon icon={UserIcon} size={18} />
            Profile
          </NavLink>
        </nav>
      </div>

      <div className="flex items-center gap-3">
        <Avatar>
          <AvatarImage src={user.imageUrl ?? undefined} alt={user.username} />
          <AvatarFallback>{user.username.charAt(0).toUpperCase()}</AvatarFallback>
        </Avatar>
        <span className="hidden text-sm font-medium sm:inline">{user.username}</span>
        <Button variant="outline" size="sm" onClick={onLogout}>
          <HugeiconsIcon icon={LogoutIcon} size={16} />
          Logout
        </Button>
      </div>
    </header>
  );
}
