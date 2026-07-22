import { HugeiconsIcon } from '@hugeicons/react';
import { UserIcon, LogoutIcon } from '@hugeicons/core-free-icons';
import { NavLink, useNavigate } from 'react-router-dom';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar.tsx';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu.tsx';
import Menu from './menu/Menu.tsx';
import type { CurrentUser } from '@/lib/api.ts';
import Logo from '@/assets/logo.webp';

type NavbarProps = {
  user: CurrentUser;
  onLogout: () => void;
};

export default function Navbar({ user, onLogout }: Readonly<NavbarProps>) {
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-40 grid grid-cols-3 items-center border-b border-border bg-background px-4 py-3 sm:px-6">
      <Menu />

      <NavLink to="/" className="justify-self-center">
        <img src={Logo} alt="Logo" className="h-12 w-auto" />
      </NavLink>

      <div className="flex items-center gap-3 justify-self-end">
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
