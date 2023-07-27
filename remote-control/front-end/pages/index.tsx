import React, { useState, useEffect } from 'react';
import axios, { AxiosError } from 'axios';
import { Socket, io } from "socket.io-client";
import { useZxing } from "react-zxing";
import { useRouter } from 'next/router';
import Head from 'next/head'
const API_SERVER = process.env.NEXT_PUBLIC_API_SERVER;
console.info(`API_SERVER: ${API_SERVER}`);

const App: React.FC = () => {
    const [command, setCommand] = useState('');
    const [message, setMessage] = useState('');
    const router = useRouter();
    // const [id, setId] = useState("");
    const id = router.query.id as string;
    console.warn(`id: ${id}`)
    const [sendCommandTrigger, setSendCommandTrigger] = useState(false);
    const [isConnected, setIsConnected] = useState(false);
    const [commands, setCommands] = useState<string[]>([]);
    const [socket, setSocket] = useState<Socket | null>(null);

    useEffect(() => {
        if (!router.isReady) {
            return;
        }
        if (id) {
            console.info(`id: ${id}`)
            const socketTemp = io(API_SERVER as string);
            const onConnect = () => {
                setIsConnected(true);
                socketTemp.emit('remote', id);
            }
            const onDisconnect = () => {
                setIsConnected(false);
            }
            const onUpdateEvent = (data: string) => {
                const jsonData = JSON.parse(data);
                setCommands(jsonData);
            }
            socketTemp.on('connect', onConnect);
            socketTemp.on('disconnect', onDisconnect);
            socketTemp.on('remote', onUpdateEvent);
            setSocket(socketTemp);
            return () => {
                socketTemp.off('connect', onConnect);
                socketTemp.off('disconnect', onDisconnect);
                socketTemp.off('remote', onUpdateEvent);
                socketTemp.disconnect();
            };
        } else {
            router.push('/scan');
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
        router.push('/scan');
    };
    return (
        <>
            <Head>
                <title>Passive Haptic Learning Remote Control</title>
            </Head>
            <div className="min-h-screen bg-gray-100 py-6 flex flex-col justify-center sm:py-12">
                <div className="relative py-3 sm:max-w-xl md:max-w-2xl lg:max-w-3xl xl:max-w-4xl mx-auto">
                    <div className="absolute inset-0 bg-gradient-to-r from-cyan-400 to-light-blue-500 shadow-lg transform -skew-y-6 sm:skew-y-0 sm:-rotate-6 sm:rounded-3xl"></div>
                    <div className="relative px-4 py-10 bg-white shadow-lg sm:rounded-3xl sm:p-10 md:p-16 lg:p-20 xl:p-24">
                        <h1 className="text-2xl md:text-3xl lg:text-4xl font-bold mb-4">PHL Remote Control</h1>
                        {id && (
                            <>
                                <p className="mb-4">ID: {id}</p>
                                <button
                                    className="bg-blue-500 text-white font-bold py-2 px-4 rounded mr-2 mb-2 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-opacity-50"
                                    onClick={rescan}
                                >
                                    Rescan
                                </button>
                                <h4 className="text-lg font-semibold mt-4 mb-2">Detected Commands:</h4>
                                {commands.length > 0 ? commands.map((c) => (<button
                                    className="bg-green-500 text-white font-bold py-2 px-4 rounded mr-2 mb-2 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-600 focus:ring-opacity-50"
                                    value={c} key={c} onClick={setAndSendCommand}>
                                    {c}
                                </button>)) : <p className="mt-4 text-red-500">No commands detected</p>}
                                {message && <p className="mt-4 text-red-500">{message}</p>}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
};

export default App;
