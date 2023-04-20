import React, { useState } from 'react';
import axios from 'axios';

const API_SERVER = 'localhost:5000';

const App: React.FC = () => {
    const [uuid, setUuid] = useState('');
    const [command, setCommand] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const sendCommand = async () => {
        try {
            await axios.post(`http://${API_SERVER}/command`, { uuid, command });
            setErrorMessage('');
        } catch (error) {
            setErrorMessage('Error sending command');
        }
    };

    return (
        <div>
            <input value={uuid} onChange={(e) => setUuid(e.target.value)} placeholder="UUID" />
            <input value={command} onChange={(e) => setCommand(e.target.value)} placeholder="Command" />
            <button onClick={sendCommand}>Send Command</button>
            {errorMessage && <p>{errorMessage}</p>}
        </div>
    );
};

export default App;
