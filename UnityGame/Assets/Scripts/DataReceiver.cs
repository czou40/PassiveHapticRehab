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

    [Obsolete("Use isUpperBodyVisible instead")]
    public bool HasPoseData { get; private set; } = false;

    public long LeftHandDataTimeStamp { get; private set; } = 0;

    public long RightHandDataTimeStamp { get; private set; } = 0;

    public long PoseDataTimeStamp { get; private set; } = 0;

    public bool isUpperBodyVisible
    {
        get
        {
            if (PoseDataTimeStamp < System.DateTimeOffset.Now.ToUnixTimeMilliseconds() - 200) // Stale data
            {
                return false;
            }
            //position 0 - 24 are upper body landmarks. their visibility should all be above the threshold
            for (int i = 0; i < 25; i++)
            {
                if (PoseVisibility[i] < visibilityThreshold)
                {
                    Debug.Log("Not all upper body landmarks are visible");
                    return false;
                }
            }
            return true;
        }
    }

    private long lastTimeUpperBodyNotVisible = 0;

    public float secondsSinceUpperBodyVisible
    {
        get
        {
            if (!isUpperBodyVisible)
            {
                return 0f;
            }
            return (System.DateTimeOffset.Now.ToUnixTimeMilliseconds() - lastTimeUpperBodyNotVisible) / 1000.0f;
        }
    }

    public float handScale = 10f;

    public float bodyScale = 1f;

    private float visibilityThreshold = 0.0f;


    const int HAND_LANDMARK_COUNT = 21;

    const int POSE_LANDMARK_COUNT = 33;

    private Thread dataUdpThread;
    private Thread imageUdpThread;
    private static int dataListenPort = 7777;
    private static int imageListenPort = 7778;

    private volatile bool _shouldStop = false;

    private void DataThreadMethod()
    {
        UdpClient dataUdpClient = new UdpClient(dataListenPort);
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

                // Handle the received data as you need
                extractData(receivedData);
            }
            catch (Exception e)
            {
                Debug.Log(e.ToString());
            }
        }
        dataUdpClient.Close();
        Debug.Log("DataThreadMethod end");
    }

    private void ImageThreadMethod()
    {
        UdpClient imageUdpClient = new UdpClient(imageListenPort);
        while (!_shouldStop)
        {
            try
            {
                if (imageUdpClient.Available <= 0) continue;
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                byte[] receiveBytes = null; // Assign a default value to receiveBytes
                while (imageUdpClient.Available > 0)
                {
                    receiveBytes = imageUdpClient.Receive(ref RemoteIpEndPoint);
                }
                this.hasImageData = true;
                this.ImageData = receiveBytes;

            }
            catch (Exception e)
            {
                Debug.Log(e.ToString());
            }
        }
        imageUdpClient.Close();
        Debug.Log("ImageThreadMethod end");
    }


    public void RequestStop()
    {
        _shouldStop = true;
    }

    private void OnEnable()
    {
        // _shouldStop = false;
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
        this.HasLeftHandData = this.HasLeftHandData || hasLeftHandData;
        this.HasRightHandData = this.HasRightHandData || hasRightHandData;
        this.HasPoseData = this.HasPoseData || hasPoseData;
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
        }
        if (!isUpperBodyVisible)
        {
            lastTimeUpperBodyNotVisible = timeStamp;
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

    // private bool checkIfUpperBodyVisible()
    // {

    // }

    public float getLeftShoulderExtensionAngle()
    {
        Vector3 leftShoulder = PosePositions[11];
        Vector3 leftWrist = PosePositions[15];
        Vector3 leftShoulderToWrist = leftWrist - leftShoulder;

        Vector3 leftHip = PosePositions[23];
        Vector3 leftShoulderToHip = leftHip - leftShoulder;

        float angle = Vector3.Angle(leftShoulderToWrist, leftShoulderToHip);
        
        Vector3 rightShoulder = PosePositions[12];
        Vector3 leftShoulderToRightShoulder = rightShoulder - leftShoulder;

        float determinant = Vector3.Dot(Vector3.Cross(leftShoulderToRightShoulder, leftShoulderToHip),leftShoulderToWrist);
        if (determinant < 0)
        {
            if (angle > 135)
            {
                angle = 360 - angle;
            }
            else
            {
                angle = -angle;
            }
        }
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

    public float getLeftAverageFingerExtensionAngle()
    {
        int[,] anglesToCalculate = {{1, 2, 3},
                                    {2, 3, 4},
                                    {0, 5, 6},
                                    {5, 6, 7},
                                    {6, 7, 8},
                                    {0, 9, 10},
                                    {9, 10, 11},
                                    {10, 11, 12},
                                    {0, 13, 14},
                                    {13, 14, 15},
                                    {14, 15, 16},
                                    {0, 17, 18},
                                    {17, 18, 19},
                                    {18, 19, 20}};
        float angleSum = 0.0f;
        for (int i = 0; i < anglesToCalculate.GetLength(0); ++i)
        {
            Vector3 lowerJoint  = LeftHandPositions[anglesToCalculate[i, 0]];
            Vector3 middleJoint = LeftHandPositions[anglesToCalculate[i, 1]];
            Vector3 upperJoint  = LeftHandPositions[anglesToCalculate[i, 2]];

            Vector3 middleToLower = lowerJoint - middleJoint;
            Vector3 middleToUpper = upperJoint - middleJoint;

            float angle = Vector3.Angle(middleToLower, middleToUpper);

            angleSum += angle;
        }

        return angleSum / anglesToCalculate.GetLength(0);
    }

    public float getLeftIndexFingerDistance()
    {
        float distance = Vector3.Distance(LeftHandPositions[4], LeftHandPositions[8]);
        // Debug.Log("4" + LeftHandPositions[4]);
        Debug.Log("distance: " + distance);
        return distance;
    }
}
