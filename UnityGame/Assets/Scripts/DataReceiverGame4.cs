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
    private bool noseTouched = false;
    private bool canSelectCrow = false;

    private Thread dataUdpThread;
    private static int dataListenPort = 5000;
    private volatile bool _shouldStop = false;
    private readonly object lockObject = new object();

    public List<long> NoseTouchTimestamps { get; private set; } = new List<long>();

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

                Debug.Log($"Received data: {receivedData}"); // Log received data
                ProcessData(receivedData);
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

    private void ProcessData(string receivedData)
    {
        lock (lockObject) // Ensure thread safety when modifying shared state
        {
            // Check if the user touched their nose
            if (receivedData.Contains("Hand is touching the nose"))
            {
                noseTouched = true;
                canSelectCrow = true; // Allow crow selection after nose is touched
                long timeStamp = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();
                NoseTouchTimestamps.Add(timeStamp);
                Debug.Log("Nose touched. You can now select a crow.");
            }
            else
            {
                noseTouched = false;
            }

            // Reset canSelectCrow if needed for the next action
            if (receivedData.Contains("Face detected in frame"))
            {
                Debug.Log("Face is detected, waiting for further input.");
            }
        }
    }

    public void OnCrowClicked()
    {
        lock (lockObject) // Ensure thread safety when accessing shared state
        {
            // Only allow the crow to be clicked if the nose was touched first
            if (canSelectCrow)
            {
                Debug.Log("Crow clicked!");
                canSelectCrow = false; // Disable further crow selection until the nose is touched again
            }
            else
            {
                Debug.Log("Please touch your nose before selecting a crow.");
            }
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