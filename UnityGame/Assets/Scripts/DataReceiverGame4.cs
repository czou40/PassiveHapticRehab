using System.Collections;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using System.IO;
using System.IO.Pipes;
using System.Text;
using System.Threading;
using System;
using System.Net;
using System.Net.Sockets;

public class DataReceiverGame4 : MonoBehaviour
{


    private Thread dataUdpThread;
    private static int dataListenPort = 5000;
    private volatile bool _shouldStop = false;
    private bool noseTouchDetected = false;
    private readonly object lockObject = new object();

    private void DataThreadMethod()
    {
        UdpClient dataUdpClient = new UdpClient(dataListenPort);
        try
        {
            while (!_shouldStop)
            {
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                byte[] receiveBytes = dataUdpClient.Receive(ref RemoteIpEndPoint); // Blocks until data is received
                string receivedData = Encoding.ASCII.GetString(receiveBytes);

                //Debug.Log($"Received data: {receivedData}"); // Log received data

                lock (lockObject)
                {
                    if (receivedData.Contains("Hand touching nose"))
                    {
                        noseTouchDetected = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Debug.Log(e.ToString());
        }
        finally
        {
            dataUdpClient.Close();
        }
    }

    public bool HasNoseTouch()
    {
        lock (lockObject)
        {
            if (noseTouchDetected)
            {
                noseTouchDetected = false;
                return true;
            }
            return false;
        }
    }

    public void RequestStop()
    {
        _shouldStop = true;
    }

    private void OnEnable()
    {
        if (dataUdpThread == null || !dataUdpThread.IsAlive)
        {
            dataUdpThread = new Thread(DataThreadMethod);
            dataUdpThread.Start();
        }
    }

    private void OnDisable()
    {
        _shouldStop = true;

        if (dataUdpThread != null && dataUdpThread.IsAlive)
        {
            dataUdpThread.Join();
            dataUdpThread = null; // Clear reference after joining
        }
    }
}
