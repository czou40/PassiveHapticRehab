import React, { useState, useEffect } from 'react';
import { useZxing } from "react-zxing";
import { Router, useRouter } from 'next/router';
import Head from 'next/head'

const ScanPage: React.FC = () => {
    const router = useRouter();
    const { ref } = useZxing({
        onResult(result) {
            const resultUrl = result.getText();
            const resultUrlTemp = resultUrl.split('id=')
            router.push('/?id=' + resultUrlTemp[resultUrlTemp.length - 1]);
        }, onError(error) {
            console.error(error);
        }
    });
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
                        <p className="mb-4">Scan QR Code</p>
                        <video ref={ref} />
                    </div>
                </div>
            </div>
        </>
    )
}

export default ScanPage;