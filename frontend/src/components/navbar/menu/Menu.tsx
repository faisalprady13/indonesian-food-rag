import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { HugeiconsIcon } from '@hugeicons/react';
import { HomeIcon, AiChat02Icon, MenuIcon, Settings02Icon } from '@hugeicons/core-free-icons';
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from '@/components/ui/sheet.tsx';
import AccordionConversation from '@/components/accordionConversation/AccordionConversation.tsx';
import SettingsDialog from '@/components/settingsDialog/SettingsDialog.tsx';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-sm font-medium transition-colors ${
    isActive
      ? 'bg-muted text-foreground'
      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
  }`;

const sectionLabelClass =
  'px-1 text-xs font-semibold tracking-wide text-muted-foreground uppercase';

export default function Menu() {
  const [open, setOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);

  return (
    <>
      <div className="justify-self-start">
        <Sheet open={open} onOpenChange={setOpen}>
          <SheetTrigger
            aria-label="Open menu"
            className="rounded-md p-2 text-muted-foreground outline-none hover:bg-muted hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            <HugeiconsIcon icon={MenuIcon} className="h-6 w-6" />
          </SheetTrigger>
          <SheetContent side="left" className="overflow-y-auto">
            <SheetTitle className="sr-only">Navigation</SheetTitle>

            <div className="flex flex-1 flex-col gap-6">
              <section className="flex flex-col gap-1">
                <h3 className={sectionLabelClass}>Menu</h3>
                <nav className="flex flex-col gap-1">
                  <NavLink to="/" end className={navLinkClass} onClick={() => setOpen(false)}>
                    <HugeiconsIcon icon={HomeIcon} size={18} className="h-4 w-4" />
                    Home
                  </NavLink>
                  <NavLink to="/chat" end className={navLinkClass} onClick={() => setOpen(false)}>
                    <HugeiconsIcon icon={AiChat02Icon} className="h-4 w-4" />
                    New Conversation
                  </NavLink>
                </nav>
              </section>

              <AccordionConversation
                enabled={open}
                onNavigate={() => setOpen(false)}
                className={sectionLabelClass}
              />

              <section className="mt-auto flex flex-col gap-1 border-t border-border pt-4">
                <button
                  type="button"
                  onClick={() => {
                    setOpen(false);
                    setSettingsOpen(true);
                  }}
                  className={
                    'flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-left text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground'
                  }
                >
                  <HugeiconsIcon icon={Settings02Icon} size={18} className="h-4 w-4" />
                  Setting
                </button>
              </section>
            </div>
          </SheetContent>
        </Sheet>
      </div>

      <SettingsDialog open={settingsOpen} onOpenChange={setSettingsOpen} />
    </>
  );
}
