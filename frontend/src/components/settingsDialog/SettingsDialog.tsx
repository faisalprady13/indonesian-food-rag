import {useActionState} from 'react';
import {useQuery, useQueryClient} from '@tanstack/react-query';
import {Moon, Sun} from 'lucide-react';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog.tsx';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select.tsx';
import {Button} from '@/components/ui/button.tsx';
import {Input} from '@/components/ui/input.tsx';
import SettingsDialogSkeleton from '@/components/skeletons/SettingsDialogSkeleton.tsx';
import {getUserSetting, updateUserSetting} from '@/lib/api';
import {applyTheme} from '@/lib/theme.ts';
import type {AppTheme} from '@/types/UserSetting.ts';

interface SettingsDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

type SettingsState = {
    error?: string;
};

const DEFAULT_APP_THEME: AppTheme = 'dark';

export default function SettingsDialog({open, onOpenChange}: SettingsDialogProps) {
    const queryClient = useQueryClient();

    const {data: userSetting, isLoading} = useQuery({
        queryKey: ['user-setting'],
        queryFn: ({signal}) => getUserSetting(signal),
        enabled: open,
        retry: false,
    });

    const appTheme: AppTheme = userSetting?.appTheme ?? DEFAULT_APP_THEME;

    const [state, submitSettings, isSaving] = useActionState<SettingsState, FormData>(
        async (_previousState, formData) => {
            const selectedTheme: AppTheme = formData.get('appTheme');
            const apiKey = String(formData.get('apiKey') ?? '').trim();

            try {
                await updateUserSetting({appTheme: selectedTheme, apiKey: apiKey || undefined});
                applyTheme(selectedTheme);
                await queryClient.invalidateQueries({queryKey: ['user-setting']});
                onOpenChange(false);
            } catch (error) {
                console.error(error);
                return {error: 'Failed to save settings'};
            }

            return {};
        },
        {},
    );

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            {isLoading ? (
                <SettingsDialogSkeleton/>
            ) : (
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Settings</DialogTitle>
                        <DialogDescription>Manage your appearance and API key preferences.</DialogDescription>
                    </DialogHeader>
                    <form key={appTheme} action={submitSettings} className="flex flex-col gap-4">
                        <div className="flex flex-col gap-1.5">
                            <label className="text-sm font-medium" htmlFor="app-theme-trigger">
                                Theme
                            </label>
                            <Select name="appTheme" defaultValue={appTheme}>
                                <SelectTrigger id="app-theme-trigger">
                                    <SelectValue/>
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="dark">
                    <span className="flex items-center gap-2">
                      <Moon size={16}/>
                      Dark
                    </span>
                                    </SelectItem>
                                    <SelectItem value="light">
                    <span className="flex items-center gap-2">
                      <Sun size={16}/>
                      Light
                    </span>
                                    </SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="flex flex-col gap-1.5">
                            <label className="text-sm font-medium" htmlFor="api-key-input">
                                OpenAI API key
                            </label>
                            <Input
                                id="api-key-input"
                                name="apiKey"
                                type="text"
                                placeholder="Enter a new API key to update it"
                                defaultValue=""
                                aria-invalid={!!state.error}
                            />
                        </div>
                        {state.error && <p className="text-sm text-destructive">{state.error}</p>}
                        <Button
                            type="submit"
                            variant="outline"
                            className="w-full border-primary text-primary hover:bg-primary/10 hover:text-primary"
                            disabled={isSaving}
                        >
                            {isSaving ? 'Saving...' : 'Save changes'}
                        </Button>
                    </form>
                </DialogContent>
            )}
        </Dialog>
    );
}
