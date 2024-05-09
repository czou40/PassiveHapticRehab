import { config, connected } from 'process';
import React, { useState, useEffect } from 'react';

function Bluetooth() {
    const [device, setDevice] = useState<BluetoothDevice|null>(null);
    const [message, setMessage] = useState('');
    const [characteristic, setCharacteristic] = useState<BluetoothRemoteGATTCharacteristic | null>(null);
    const [log, setLog] = useState('');

    async function scanForConnectedDevices() {
        try {
            const devices = await navigator.bluetooth.getDevices();
            const connectedDevice = devices.find(device => {
                console.log(device.gatt)
                return device.gatt.connected && isDesiredDevice(device)});
            if (connectedDevice) {
                setDevice(connectedDevice);
                setLog(`Connected device found: ${connectedDevice.name}`);
                await connect();
            } else {
                setLog('No connected device found');
            }
        } catch (error) {
            setLog(`Error: ${(error as Error).message}`);
        }
    }

    function isDesiredDevice(device: BluetoothDevice) {
        // Replace this condition with the desired condition to identify your device
        return device.name?.startsWith('Pixel');
    }

    useEffect(() => {
        scanForConnectedDevices();
    }, []);



    async function requestDevice() {
        try {
            const device = await navigator.bluetooth.requestDevice({
                filters: [{ services: ['00001112-0000-1000-8000-00805f9b34fb']}],
                optionalServices: ['00001112-0000-1000-8000-00805f9b34fb', '0000111f-0000-1000-8000-00805f9b34fb']
            });
            // const device = await navigator.bluetooth.requestDevice({acceptAllDevices: true});
            setDevice(device);
            console.log(device);
            setLog(`Connected to device: ${device.name}`);
        } catch (error) {
            setLog(`Error: ${(error as Error).message}`);
        }
    }

    function isDeviceConnected() {
        return device && device.gatt.connected;
    }

    async function connect() {
        console.log(2222)
        if (!device) {
            setLog('No device selected');
            return;
        }

        if (isDeviceConnected()) {
            setLog('Device is already connected');
            return;
        }

        try {
            console.warn('3257877664fdsnjkfsdnjk')
            const server = await device.gatt.connect();
            console.warn('vfddffdvdf')

            const service = await server.getPrimaryService('00001112-0000-1000-8000-00805f9b34fb');
            console.warn('xascdsvds')

            const _characteristic = await service.getCharacteristic('0000111f-0000-1000-8000-00805f9b34fb');
            console.warn(_characteristic);
            setCharacteristic(_characteristic); // Save characteristic

            device.addEventListener('gattserverdisconnected', () => {
                setLog('Device disconnected');
            });

            setMessage(`gatt connected: ${device.name}`);
        } catch (error) {
            console.error(error);
            setLog(`Error: ${(error as Error).message}`);
        }
    }

    async function disconnect() {
        if (!device || !device.gatt.connected) {
            setLog('Device not connected');
            return;
        }

        device.gatt.disconnect();
    }

    async function sendData(value: string) {
        if (!characteristic) {
            setLog('Characteristic not available');
            return;
        }

        try {
            const data = new TextEncoder().encode(value);
            await characteristic.writeValueWithResponse(data);
            setLog(`Sent: ${value}`);
        } catch (error) {
            setLog(`Error: ${(error as Error).message}`);
        }
    }

    return (
        <div>
            <button onClick={requestDevice}>Request Device</button>
            <button onClick={connect}>Connect</button>
            <button onClick={disconnect}>Disconnect</button>
            <button onClick={() => sendData("1")}>Send 1</button>
            <button onClick={() => sendData("0")}>Send 0</button>
            <div>{isDeviceConnected() ? "Device is connected!":"Device not connected."}</div>
            <div>{message}</div>
            <div>{log}</div>
        </div>
    );
}

export default Bluetooth;
