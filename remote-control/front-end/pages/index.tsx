import React, { useState, useEffect } from 'react';
import axios, { AxiosError } from 'axios';
import { useZxing } from "react-zxing";
import { useRouter } from 'next/router';
import { time } from 'console';
const API_SERVER = 'http://localhost:5000';

const App: React.FC = () => {
    const [command, setCommand] = useState('');
    const [message, setMessage] = useState('');
    const router = useRouter();
    const [id, setId] = useState("");
    const [sendCommandTrigger, setSendCommandTrigger] = useState(false);


    const { ref } = useZxing({
        onResult(result) {
            setId(result.getText());
        }, onError(error) {
            console.error(error);
        }
    });

    useEffect(() => {
        if (router.query.id) {
            setId(router.query.id as string);
        }
    }, [router.query.id, router]);

    console.log(id);

    useEffect(() => {
        if (id && id !== router.query.id) {
            router.push({
                pathname: router.pathname,
                query: { ...router.query, id: id }
            });
        }
    }, [id]);

    useEffect(() => {
        if (sendCommandTrigger) {
            sendCommand();
            setSendCommandTrigger(false);
        }
    }, [command, sendCommandTrigger]);

    const setAndSendCommand = (e: React.MouseEvent) => {
        setCommand((e.currentTarget as HTMLButtonElement).value);
        setSendCommandTrigger(true);
    };

    const sendCommand = async () => {
        try {
            await axios.post(`${API_SERVER}/command`, { id, command });
            setMessage('Command sent');
        } catch (error) {
            console.log(error);
            if (axios.isAxiosError(error)) {
                const axiosError: AxiosError = error;
                if (axiosError.response) {
                    if (axiosError.response.data) {
                        setMessage(axiosError.response.data as string);
                    }
                } else if (axiosError.message) {
                    setMessage(axiosError.message);
                }
            } else {
                const e = error as Error;
                setMessage(e.toString());
            }
        }
    };

    const rescan = () => {
        setId("");
        router.push({
            pathname: router.pathname,
            query: { ...router.query, id: undefined }
        });
    };

    const commonCommands = ['Forward', 'Backward', 'Yes', 'No', 'Uncertain', 'Start', 'Stop', 'Exit', 'Start Spasticity Test', 'Start Tactile Sensation Test', 'View My Progress'];

    return (
        <div className="min-h-screen bg-gray-100 py-6 flex flex-col justify-center sm:py-12">
            <div className="relative py-3 sm:max-w-xl md:max-w-2xl lg:max-w-3xl xl:max-w-4xl mx-auto">
                <div className="absolute inset-0 bg-gradient-to-r from-cyan-400 to-light-blue-500 shadow-lg transform -skew-y-6 sm:skew-y-0 sm:-rotate-6 sm:rounded-3xl"></div>
                <div className="relative px-4 py-10 bg-white shadow-lg sm:rounded-3xl sm:p-10 md:p-16 lg:p-20 xl:p-24">
                    <h1 className="text-2xl md:text-3xl lg:text-4xl font-bold mb-4">PHL Remote Control</h1>
                    {!id && (
                        <>
                            <p className="mb-4">Scan QR Code</p>
                        </>
                    )}
                    <video ref={ref} style={{ display: id ? 'none' : 'revert' }} />
                    {id && (
                        <>
                            <p className="mb-4">ID: {id}</p>
                            <button
                                className="bg-blue-500 text-white font-bold py-2 px-4 rounded mr-2 mb-2 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-opacity-50"
                                onClick={rescan}
                            >
                                Rescan
                            </button>
                            <input
                                value={command}
                                onChange={(e) => setCommand(e.target.value)}
                                placeholder="Command"
                                className="border-2 border-gray-300 rounded w-full p-2 mb-4"
                            />
                            <button
                                className="bg-green-500 text-white font-bold py-2 px-4 rounded mr-2 mb-2 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-600 focus:ring-opacity-50"
                                onClick={sendCommand}
                            >
                                Send Command
                            </button>
                            <h4 className="text-lg font-semibold mt-4 mb-2">Common Commands:</h4>
                            {commonCommands.map((c) => (<button
                                className="bg-green-500 text-white font-bold py-2 px-4 rounded mr-2 mb-2 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-600 focus:ring-opacity-50"
                                value={c} key={c} onClick={setAndSendCommand}>
                                {c}
                            </button>))}
                            {message && <p className="mt-4 text-red-500">{message}</p>}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default App;
