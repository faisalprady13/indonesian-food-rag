import './App.css';
import { Navigate, Route, Routes } from 'react-router-dom';
import { LoginForm } from '@/components/login-form.tsx';
import { SignupForm } from '@/components/signup-form.tsx';

function App() {
  return (
    <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
      <div className="w-full max-w-sm">
        <Routes>
          <Route path="/login" element={<LoginForm />} />
          <Route path="/register" element={<SignupForm />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
