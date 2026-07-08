import {useState} from 'react';
import {Button} from '@/components/ui/button';
import './App.css';

function App() {
    const [count, setCount] = useState(0);

    return (

        <section id="center">
            <p>Count is {count}</p>
            <Button onClick={() => setCount((count) => count + 1)}>shadcn/ui Button</Button>
        </section>

    );
}

export default App;
