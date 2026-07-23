import './App.css';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { LoginForm } from '@/pages/auth/login-form.tsx';
import { SignupForm } from '@/pages/auth/signup-form.tsx';
import MyProfile from '@/pages/profile/my-profile.tsx';
import Chat from '@/pages/chat/chat.tsx';
import Home from '@/pages/Home.tsx';
import AppLayout from '@/layout/AppLayout.tsx';
import { useEffect } from 'react';
import ProtectedRoute from '@/components/protectedRoute/ProtectedRoute.tsx';
import { getMe, setToken, logout } from '@/lib/api';
import { useAppStore } from '@/store/appStore.ts';

function App() {
  const user = useAppStore((state) => state.user);
  const setUser = useAppStore((state) => state.setUser);
  const queryClient = useQueryClient();

  async function refreshUser() {
    const currentUser = await getMe();
    setUser(currentUser);
    queryClient.setQueryData(['me'], currentUser);
    return currentUser;
  }

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
      setToken(token);
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  const { data: me, isError } = useQuery({ queryKey: ['me'], queryFn: getMe, retry: false });

  useEffect(() => {
    if (me) {
      setUser(me);
    } else if (isError) {
      setUser(null);
    }
  }, [me, isError, setUser]);

  function handleLogout() {
    logout();
    setUser(null);
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={
          <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
            <div className="w-full max-w-sm">
              <LoginForm onLoginSuccess={refreshUser} />
            </div>
          </div>
        }
      />
      <Route
        path="/register"
        element={
          <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
            <div className="w-full max-w-sm">
              <SignupForm onLoginSuccess={refreshUser} />
            </div>
          </div>
        }
      />
      <Route element={<ProtectedRoute user={user} />}>
        <Route element={<AppLayout user={user!} onLogout={handleLogout} />}>
          <Route index element={<Home />} />
          <Route path="/profile" element={<MyProfile user={user!} />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/chat/:conversationId" element={<Chat />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

export default App;
