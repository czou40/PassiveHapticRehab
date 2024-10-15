using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using System.Linq;

using UnityEngine;

using System.IO;
using System.IO.Pipes;
using System.Text;
using System.Threading;

using System;
using System.Net;
using System.Net.Sockets;


public class FaceCaptureDataReceiver : MonoBehaviour
{
    public Vector3[] LeftHandPositions { get; private set; } = new Vector3[HAND_LANDMARK_COUNT];
    public Vector3[] RightHandPositions { get; private set; } = new Vector3[HAND_LANDMARK_COUNT];
    public Vector3[] PosePositions { get; private set; } = new Vector3[POSE_LANDMARK_COUNT];

    public float[] LeftHandVisibility { get; private set; } = Enumerable.Repeat(1.0f, HAND_LANDMARK_COUNT).ToArray();
    public float[] RightHandVisibility { get; private set; } = Enumerable.Repeat(1.0f, HAND_LANDMARK_COUNT).ToArray();
    public float[] PoseVisibility { get; private set; } = Enumerable.Repeat(1.0f, POSE_LANDMARK_COUNT).ToArray();

    public bool HasFaceData { get; private set; } = false;

    private const int HAND_LANDMARK_COUNT = 21;
    private const int POSE_LANDMARK_COUNT = 33;

    private long PoseDataTimeStamp { get; set; } = 0;
    private float visibilityThreshold = 0.5f; // Adjust threshold as needed

    // Assuming the index for the nose is 0 (update if necessary)
    private const int NOSE_INDEX = 0;

    private static int dataListenPort = 5000;
    private static int imageListenPort = 5001;

    private volatile bool _shouldStop = false;
    private Thread dataUdpThread;
    private Thread imageUdpThread;

    public byte[] ImageData { get; private set; } = null;

    public bool hasImageData { get; private set; } = false;

    // private WebCamTexture webcamTexture;
    // public Button continueButton;
    // public RawImage cameraImage;
    // public RectTransform faceFrame;
    
    // private UdpClient udpClient;
    // private IPEndPoint endPoint;

    void Start() {
        DataThreadMethod();
    }

    public void RequestStop()
    {
        _shouldStop = true;
    }

    private void OnEnable()
    {
        // _shouldStop = false;
        Debug.Log("OnEnable");
        if (dataUdpThread == null || !dataUdpThread.IsAlive)
        {
            dataUdpThread = new Thread(DataThreadMethod);
            dataUdpThread.Start();
        }

        if (imageUdpThread == null || !imageUdpThread.IsAlive)
        {
            imageUdpThread = new Thread(ImageThreadMethod);
            imageUdpThread.Start();
        }
    }

    private void OnDisable()
    {
        Debug.Log("OnDisable");
        _shouldStop = true;

        if (imageUdpThread != null && imageUdpThread.IsAlive)
        {
            imageUdpThread.Join();
        }
        if (dataUdpThread != null && dataUdpThread.IsAlive)
        {
            dataUdpThread.Join();
        }
        Debug.Log("OnDisable end");
    }
    

    private void DataThreadMethod()
{
    using (UdpClient dataUdpClient = new UdpClient(dataListenPort))
    {
        while (!_shouldStop)
        {
            try
            {
                if (dataUdpClient.Available <= 0) continue;
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                byte[] receiveBytes = null;
                while (dataUdpClient.Available > 0)
                {
                    receiveBytes = dataUdpClient.Receive(ref RemoteIpEndPoint);
                }
                string receivedData = Encoding.ASCII.GetString(receiveBytes);

                Debug.Log(receivedData);
                extractData(receivedData);
            }
            catch (Exception e)
            {
                Debug.Log(e.ToString());
            }
        }
        Debug.Log("DataThreadMethod end");
    }
}


    // private void ImageThreadMethod()
    // {
    //     UdpClient imageUdpClient = new UdpClient(imageListenPort);
    //     while (!_shouldStop)
    //     {
    //         try
    //         {
    //             if (imageUdpClient.Available <= 0) continue;
    //             IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
    //             byte[] receiveBytes = null; // Assign a default value to receiveBytes
    //             while (imageUdpClient.Available > 0)
    //             {
    //                 receiveBytes = imageUdpClient.Receive(ref RemoteIpEndPoint);
    //             }
    //             this.hasImageData = true;
    //             this.ImageData = receiveBytes;

    //         }
    //         catch (Exception e)
    //         {
    //             Debug.Log(e.ToString());
    //         }
    //     }
    //     imageUdpClient.Close();
    //     Debug.Log("ImageThreadMethod end");
    // }

     public bool IsFaceVisible
    {
        get
        {
            // Check if the nose visibility is above the threshold
            return PoseVisibility[NOSE_INDEX] >= visibilityThreshold && PoseDataTimeStamp >= System.DateTimeOffset.Now.ToUnixTimeMilliseconds() - 200;
        }
    }

    public bool IsNoseTouched(Vector3 leftHandPosition, Vector3 rightHandPosition)
    {
        // Define a threshold distance to consider a "touch"
        float touchThreshold = 0.1f; // Adjust as necessary

        Vector3 nosePosition = PosePositions[NOSE_INDEX];

        // Check if either hand is close enough to the nose position
        return Vector3.Distance(leftHandPosition, nosePosition) < touchThreshold || Vector3.Distance(rightHandPosition, nosePosition) < touchThreshold;
    }

    private void extractData(string str)
    {
        Vector3[] positions;
        float[] visibility;
        string[] lines = str.Split('\n');
        long timeStamp = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();

        Debug.Log("In Extract Data");

        foreach (string l in lines)
        {
            Debug.Log(l);
            if (l.Trim().Length == 0) continue;

            try
            {
                string[] s = l.Split('|');
                if (s[0] == "Pose")
                {
                    positions = PosePositions;
                    HasFaceData = true;

                    // Check visibility
                    for (int i = 0; i < POSE_LANDMARK_COUNT; i++)
                    {
                        if (!float.TryParse(s[i + 1], out PoseVisibility[i]) || PoseVisibility[i] < 0 || PoseVisibility[i] > 1)
                        {
                            PoseVisibility[i] = 1.0f; // Default to fully visible
                        }
                    }
                }
                // Other data extraction logic...

                PoseDataTimeStamp = timeStamp; // Update timestamp when pose data is received
            }
            catch (System.Exception e)
            {
                Debug.LogError(e);
            }
        }
    }

    // void Start()
    // {
    //     // Start the webcam
    //     WebCamDevice[] devices = WebCamTexture.devices;
    //     if (devices.Length > 0)
    //     {
    //         webcamTexture = new WebCamTexture(devices[0].name);
    //         cameraImage.texture = webcamTexture;
    //         webcamTexture.Play();
    //     }

    //     // Set up the UDP client to receive data from Python
    //     udpClient = new UdpClient(5005);
    //     endPoint = new IPEndPoint(IPAddress.Parse("127.0.0.1"), 5005);

    //     // Start listening for data from Python
    //     InvokeRepeating("ReceiveData", 0, 0.1f);  // Check every 100ms
    // }

    // void ReceiveData()
    // {
    //     try
    //     {
    //         if (udpClient.Available > 0)
    //         {
    //             byte[] data = udpClient.Receive(ref endPoint);
    //             string message = Encoding.UTF8.GetString(data);

    //             if (message == "1")
    //             {
    //                 FaceDetected();
    //             }
    //             else if (message == "0")
    //             {
    //                 FaceNotDetected();
    //             }
    //         }
    //     }
    //     catch (SocketException ex)
    //     {
    //         Debug.LogError($"Socket error: {ex.Message}");
    //     }
    // }

    // void Update()
    // {
    //     // The button is only interactable if the face is detected
    //     continueButton.interactable = isFaceInsideFrame;
    // }

    // private bool isFaceInsideFrame = false;

    // public void FaceDetected()
    // {
    //     isFaceInsideFrame = true;
    // }

    // public void FaceNotDetected()
    // {
    //     isFaceInsideFrame = false;
    // }

    void OnDestroy()
    {
        // if (webcamTexture != null && webcamTexture.isPlaying)
        // {
        //     webcamTexture.Stop();
        // }

        // // Close the UDP client when the program ends
        // udpClient.Close();
    }
}
