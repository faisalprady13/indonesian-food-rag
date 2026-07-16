import { HugeiconsIcon } from '@hugeicons/react';
import { HomeIcon, UserIcon, LogoutIcon } from '@hugeicons/core-free-icons';
import { NavLink, useNavigate } from 'react-router-dom';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar.tsx';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu.tsx';
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
  const navigate = useNavigate();

  return (
    <header className="flex items-center justify-between border-b border-border px-4 py-3 sm:px-6">
      <div className="flex items-center gap-6">
        <NavLink to="/">
          <img src={Logo} alt="Logo" className="h-8 w-auto" />
        </NavLink>
        <nav className="flex items-center gap-1">
          <NavLink to="/" end className={navLinkClass}>
            <HugeiconsIcon icon={HomeIcon} size={18} />
            Home
          </NavLink>
        </nav>
      </div>

      <div className="flex items-center gap-3">
        <DropdownMenu>
          <DropdownMenuTrigger className="flex items-center gap-2 rounded-full px-2 py-1 text-muted-foreground transition-colors outline-none hover:bg-muted hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50 aria-expanded:bg-muted aria-expanded:text-foreground">
            <Avatar>
              <AvatarImage src={user.imageUrl ?? undefined} alt={user.username} />
              <AvatarFallback>{user.username.charAt(0).toUpperCase()}</AvatarFallback>
            </Avatar>
            <span className="hidden text-sm font-medium sm:inline">{user.username}</span>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem onClick={() => navigate('/profile')}>
              <HugeiconsIcon icon={UserIcon} size={16} />
              Profile
            </DropdownMenuItem>
            <DropdownMenuItem onClick={onLogout} className="text-red-500">
              <HugeiconsIcon icon={LogoutIcon} size={16} className="text-red-500" />
              Logout
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
