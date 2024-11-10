using UnityEngine;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System;

public class DataReceiverFingerCoord : MonoBehaviour
{
    // Network configuration
    private const int ListenPort = 5000;
    private const int BufferSize = 1024;

    // Threading
    private Thread dataThread;
    private volatile bool shouldStop;
    private readonly object lockObject = new object();

    // Finger touch data
    private int currentTouchedFinger;
    private bool isDataUpdated;

    /// <summary>
    /// Represents which finger is currently touching the thumb
    /// 0 = No touch, 1 = Index, 2 = Middle, 3 = Ring, 4 = Pinky
    /// </summary>
    public int TouchedFinger
    {
        get
        {
            lock (lockObject)
            {
                isDataUpdated = false;
                return currentTouchedFinger;
            }
        }
    }

    /// <summary>
    /// Check if new finger touch data has arrived
    /// </summary>
    public bool HasNewData
    {
        get
        {
            lock (lockObject)
            {
                return isDataUpdated;
            }
        }
    }

    private void DataThreadMethod()
    {
        UdpClient udpClient = new UdpClient(ListenPort);
        IPEndPoint remoteEndPoint = new IPEndPoint(IPAddress.Any, 0);

        try
        {
            while (!shouldStop)
            {
                try
                {
                    // Receive data from Python script
                    byte[] receiveBytes = udpClient.Receive(ref remoteEndPoint);
                    string receivedData = Encoding.ASCII.GetString(receiveBytes);

                    // Parse the received finger touch data
                    if (int.TryParse(receivedData, out int touchedFinger))
                    {
                        lock (lockObject)
                        {
                            currentTouchedFinger = touchedFinger;
                            isDataUpdated = true;
                        }
                    }
                }
                catch (SocketException e)
                {
                    Debug.LogError($"Socket error: {e.Message}");
                }
            }
        }
        finally
        {
            udpClient.Close();
        }
    }

    #region Unity Lifecycle Methods

    private void OnEnable()
    {
        shouldStop = false;
        if (dataThread == null || !dataThread.IsAlive)
        {
            dataThread = new Thread(DataThreadMethod)
            {
                IsBackground = true
            };
            dataThread.Start();
        }
    }

    private void OnDisable()
    {
        // Clean shutdown of the thread
        shouldStop = true;
        if (dataThread != null && dataThread.IsAlive)
        {
            dataThread.Join(1000); // Wait up to 1 second for thread to finish
            if (dataThread.IsAlive)
            {
                dataThread.Abort(); // Force abort if thread doesn't finish
            }
            dataThread = null;
        }
    }

    private void OnDestroy()
    {
        OnDisable();
    }

    #endregion

    #region Public Methods

    /// <summary>
    /// Returns true if the specified finger is currently touching the thumb
    /// </summary>
    public bool IsFingerTouching(int fingerNumber)
    {
        lock (lockObject)
        {
            return currentTouchedFinger == fingerNumber;
        }
    }

    /// <summary>
    /// Returns true if any finger is touching the thumb
    /// </summary>
    public bool IsAnyFingerTouching()
    {
        lock (lockObject)
        {
            return currentTouchedFinger != 0;
        }
    }

    #endregion
}