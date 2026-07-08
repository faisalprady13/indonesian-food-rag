import './App.css';
import {LoginForm} from "@/components/login-form.tsx";

function App() {

  return (
    <div className="flex max-h-svh flex-col items-center justify-center bg-muted p-6 md:p-10">
      <div className="w-full max-w-sm md:max-w-3xl">
        <LoginForm/>
      </div>
    </div>

  );
}

export default App;
