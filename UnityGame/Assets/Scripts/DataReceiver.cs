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


public class DataReceiver : MonoBehaviour
{

    public Vector3[] LeftHandPositions { get; private set; } = new Vector3[HAND_LANDMARK_COUNT];

    public Vector3[] RightHandPositions { get; private set; } = new Vector3[HAND_LANDMARK_COUNT];

    public Vector3[] PosePositions { get; private set; } = new Vector3[POSE_LANDMARK_COUNT];

    public float[] LeftHandVisibility { get; private set; } = Enumerable.Repeat(1.0f, HAND_LANDMARK_COUNT).ToArray();

    public float[] RightHandVisibility { get; private set; } = Enumerable.Repeat(1.0f, HAND_LANDMARK_COUNT).ToArray();

    public float[] PoseVisibility { get; private set; } = Enumerable.Repeat(1.0f, POSE_LANDMARK_COUNT).ToArray();

    public byte[] ImageData { get; private set; } = null;

    public bool hasImageData { get; private set; } = false;

    public bool HasLeftHandData { get; private set; } = false;

    public bool HasRightHandData { get; private set; } = false;

    public bool HasPoseData { get; private set; } = false;

    public long LeftHandDataTimeStamp { get; private set; } = 0;

    public long RightHandDataTimeStamp { get; private set; } = 0;

    public long PoseDataTimeStamp { get; private set; } = 0;

    public float handScale = 10f;

    public float bodyScale = 1f;


    const int HAND_LANDMARK_COUNT = 21;

    const int POSE_LANDMARK_COUNT = 33;

    private Thread dataUdpThread;
    private Thread imageUdpThread;
    private UdpClient dataUdpClient;
    private UdpClient imageUdpClient;
    private int dataListenPort = 7777;
    private int imageListenPort = 7778;


    private void Start()
    {
        dataUdpThread = new Thread(new ThreadStart(DataThreadMethod))
        {
            IsBackground = true
        };
        dataUdpThread.Start();

        imageUdpThread = new Thread(new ThreadStart(ImageThreadMethod))
        {
            IsBackground = true
        };
        imageUdpThread.Start();
    }

    private void extractData(string str)
    {
        Vector3[] positions;
        float[] visibility;
        string[] lines = str.Split('\n');
        bool hasLeftHandData = false;
        bool hasRightHandData = false;
        bool hasPoseData = false;
        long timeStamp = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();
        foreach (string l in lines)
        {
            if (l.Trim().Length == 0) continue;
            try
            {
                string[] s = l.Split('|');
                
                int i;
                float multiplier;
                bool isPoseData = false;
                if (s[0].StartsWith("Visibility"))
                {
                    if (s[0] == "VisibilityLeft")
                    {
                        visibility = LeftHandVisibility;
                        isPoseData = false;
                    }
                    else if (s[0] == "VisibilityRight")
                    {
                        visibility = RightHandVisibility;
                        isPoseData = false;
                    }
                    else if (s[0] == "VisibilityPose")
                    {
                        visibility = PoseVisibility;
                        isPoseData = true;
                    }
                    else
                    {
                        Debug.LogWarning("Invalid data type: " + s[0]);
                        continue;
                    }
                    if (isPoseData && s.Length < POSE_LANDMARK_COUNT + 1 || !isPoseData && s.Length < HAND_LANDMARK_COUNT + 1)
                    {
                        Debug.LogWarning("Invalid landmark count");
                        continue;
                    }
                    for (i = 0; i < (isPoseData ? POSE_LANDMARK_COUNT : HAND_LANDMARK_COUNT); i++)
                    {
                        if (!float.TryParse(s[i + 1], out visibility[i]) || visibility[i] < 0 || visibility[i] > 1)
                        {
                            Debug.LogWarning("Invalid visibility value: " + s[i + 1]);
                            visibility[i] = 1.0f;
                            continue;
                        }
                    }
                }
                else
                {
                    if (s.Length < 5) continue;
                    if (s[0] == "Left")
                    {
                        positions = LeftHandPositions;
                        hasLeftHandData = true;
                        multiplier = handScale;
                    }
                    else if (s[0] == "Right")
                    {
                        positions = RightHandPositions;
                        hasRightHandData = true;
                        multiplier = handScale;
                    }
                    else if (s[0] == "Pose")
                    {
                        positions = PosePositions;
                        hasPoseData = true;
                        multiplier = bodyScale;
                        isPoseData = true;
                    }
                    else
                    {
                        Debug.LogWarning("Invalid data type: " + s[0]);
                        continue;
                    }
                    if (!int.TryParse(s[1], out i) || i < 0 || isPoseData ? i >= POSE_LANDMARK_COUNT : i >= HAND_LANDMARK_COUNT)
                    {
                        Debug.LogWarning("Invalid landmark index");
                        continue;
                    }
                    positions[i] = new Vector3(float.Parse(s[2]), float.Parse(s[3]), float.Parse(s[4])) * multiplier;
                }

            }
            catch (System.Exception e)
            {
                Debug.LogError(e);
                hasLeftHandData = false;
                hasRightHandData = false;
                hasPoseData = false;
            }
        }
        this.HasLeftHandData = hasLeftHandData;
        this.HasRightHandData = hasRightHandData;
        this.HasPoseData = hasPoseData;
        if (hasLeftHandData)
        {
            this.LeftHandDataTimeStamp = timeStamp;
        }
        if (hasRightHandData)
        {
            this.RightHandDataTimeStamp = timeStamp;
        }
        if (hasPoseData)
        {
            this.PoseDataTimeStamp = timeStamp;
            Debug.Log(string.Join('|', PoseVisibility.Select(x => x.ToString()).ToArray()));
        }
    }

    private void DataThreadMethod()
    {
        if (dataUdpClient != null)
        {
            dataUdpClient.Close();
        }
        dataUdpClient = new UdpClient(dataListenPort);
        while (true)
        {
            try
            {
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                byte[] receiveBytes = dataUdpClient.Receive(ref RemoteIpEndPoint);
                string receivedData = Encoding.ASCII.GetString(receiveBytes);

                // Handle the received data as you need
                extractData(receivedData);
            }
            catch (Exception e)
            {
                Debug.Log(e.ToString());
            }
        }
    }

    private void ImageThreadMethod()
    {
        imageUdpClient = new UdpClient(imageListenPort);
        while (true)
        {
            try
            {
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                byte[] receiveBytes = imageUdpClient.Receive(ref RemoteIpEndPoint);

                this.hasImageData = true;
                this.ImageData = receiveBytes;

            }
            catch (Exception e)
            {
                Debug.Log(e.ToString());
            }
        }
    }

    void OnDestroy()
    {
        if (dataUdpThread != null && dataUdpThread.IsAlive)
        {
            dataUdpThread.Abort();
        }

        if (dataUdpClient != null)
        {
            dataUdpClient.Close();
        }

        if (imageUdpThread != null && imageUdpThread.IsAlive)
        {
            imageUdpThread.Abort();
        }

        if (imageUdpClient != null)
        {
            imageUdpClient.Close();
        }
    }

    public float getLeftShoulderExtensionAngle()
    {
        Vector3 leftShoulder = PosePositions[11];
        Vector3 leftWrist = PosePositions[15];
        Vector3 leftShoulderToWrist = leftWrist - leftShoulder;

        Vector3 leftHip = PosePositions[23];
        Vector3 leftShoulderToHip = leftHip - leftShoulder;

        float angle = Vector3.Angle(leftShoulderToWrist, leftShoulderToHip);

        return angle;
    }

    public float getLeftShoulderRotationAngle()
    {
        Vector3 leftShoulder = PosePositions[11];
        Vector3 rightShoulder = PosePositions[12];
        Vector3 leftShoulderToRightShoulder = rightShoulder - leftShoulder;
        Vector3 leftElbow = PosePositions[13];
        Vector3 leftWrist = PosePositions[15];
        Vector3 leftElbowToWrist = leftWrist - leftElbow;
        float angle = Math.Max(0, Vector3.Angle(leftShoulderToRightShoulder, leftElbowToWrist) - 45);
        // float angle = Vector3.Angle(leftShoulderToRightShoulder, leftElbowToWrist) - 45;
        return angle;
    }

    public float getLeftWristExtensionAngle()
    {
        Vector3 leftWrist = PosePositions[15];
        Vector3 leftElbow = PosePositions[13];
        Vector3 leftWristToElbow = leftElbow - leftWrist;

        Vector3 leftMiddleFingerMcp = LeftHandPositions[9];
        Vector3 leftWristInHand = LeftHandPositions[0];
        Vector3 leftWristToMiddleFingerMcp = leftMiddleFingerMcp - leftWristInHand;

        float angle = Vector3.Angle(leftWristToElbow, leftWristToMiddleFingerMcp);

        return angle;
    }
}
