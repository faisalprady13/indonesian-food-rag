import './App.css';
import { Navigate, Route, Routes } from 'react-router-dom';
import { LoginForm } from '@/pages/auth/login-form.tsx';
import { SignupForm } from '@/pages/auth/signup-form.tsx';
import MyProfile from '@/pages/profile/my-profile.tsx';
import Home from '@/pages/Home.tsx';
import AppLayout from '@/layout/AppLayout.tsx';
import { useState, useEffect } from 'react';
import ProtectedRoute from './components/protectedRoute/ProtectedRoute.tsx';
import { getMe, setToken, logout, type CurrentUser } from '@/lib/api';

function App() {
  const [user, setUser] = useState<CurrentUser | undefined | null>(undefined);

  async function refreshUser() {
    const currentUser = await getMe();
    setUser(currentUser);
    return currentUser;
  }

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
      setToken(token);
      window.history.replaceState({}, '', window.location.pathname);
    }
    getMe()
      .then(setUser)
      .catch(() => setUser(null));
  }, []);

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
          <Route index element={<Home user={user!} />} />
          <Route path="/profile" element={<MyProfile user={user!} />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

export default App;
