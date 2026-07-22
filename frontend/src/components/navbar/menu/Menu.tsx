import { useState } from 'react';
import { NavLink, useNavigate, useParams } from 'react-router-dom';
import { HugeiconsIcon } from '@hugeicons/react';
import { HomeIcon, AiChat02Icon, MenuIcon, Settings02Icon } from '@hugeicons/core-free-icons';
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from '@/components/ui/sheet.tsx';
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionPanel,
} from '@/components/ui/accordion.tsx';
import SettingsDialog from '@/components/settingsDialog/SettingsDialog.tsx';
import { useAsyncData } from '@/hooks/useAsyncData.ts';
import { getConversations } from '@/lib/mockChatApi.ts';
import type { Conversation } from '@/types/Chat.ts';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-sm font-medium transition-colors ${
    isActive
      ? 'bg-muted text-foreground'
      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
  }`;

const sectionLabelClass =
  'px-1 text-xs font-semibold tracking-wide text-muted-foreground uppercase';

const menuActionClass =
  'flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-left text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground';

export default function Menu() {
  const [open, setOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const navigate = useNavigate();
  const { conversationId } = useParams<{ conversationId: string }>();

  const [conversations, loadingConversations] = useAsyncData<Conversation[]>(
    (signal) => getConversations(signal),
    [],
    open,
  );

  function goToConversation(id: number) {
    navigate(`/chat/${id}`);
    setOpen(false);
  }

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
                <h3 className={sectionLabelClass}>Pages</h3>
                <nav className="flex flex-col gap-1">
                  <NavLink to="/" end className={navLinkClass} onClick={() => setOpen(false)}>
                    <HugeiconsIcon icon={HomeIcon} size={18} className="h-4 w-4" />
                    Home
                  </NavLink>
                  <NavLink to="/chat" end className={navLinkClass} onClick={() => setOpen(false)}>
                    <HugeiconsIcon icon={AiChat02Icon} className="h-4 w-4" />
                    Chat
                  </NavLink>
                </nav>
              </section>

              <Accordion defaultValue={['conversations']}>
                <AccordionItem value="conversations">
                  <AccordionTrigger className={sectionLabelClass}>Conversations</AccordionTrigger>
                  <AccordionPanel>
                    <div className="flex flex-col gap-1">
                      {loadingConversations ? (
                        <p className="px-2.5 py-1.5 text-sm text-muted-foreground">Loading…</p>
                      ) : conversations && conversations.length > 0 ? (
                        conversations.map((conversation) => {
                          const isActive = conversationId === String(conversation.id);
                          return (
                            <button
                              key={conversation.id}
                              type="button"
                              onClick={() => goToConversation(conversation.id)}
                              className={`truncate rounded-md px-2.5 py-1.5 text-left text-sm transition-colors ${
                                isActive
                                  ? 'bg-muted text-foreground'
                                  : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                              }`}
                            >
                              {conversation.title}
                            </button>
                          );
                        })
                      ) : (
                        <p className="px-2.5 py-1.5 text-sm text-muted-foreground">
                          No conversations yet.
                        </p>
                      )}
                    </div>
                  </AccordionPanel>
                </AccordionItem>
              </Accordion>

              <section className="mt-auto flex flex-col gap-1 border-t border-border pt-4">
                <button
                  type="button"
                  onClick={() => {
                    setOpen(false);
                    setSettingsOpen(true);
                  }}
                  className={menuActionClass}
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
