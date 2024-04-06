using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
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

    public bool HasLeftHandData { get; private set; } = false;

    public bool HasRightHandData { get; private set; } = false;

    public bool HasPoseData { get; private set; } = false;

    public float handScale = 10f;

    public float bodyScale = 1f;


    const int HAND_LANDMARK_COUNT = 21;

    const int POSE_LANDMARK_COUNT = 33;

    private Thread udpThread;
    private UdpClient udpClient;
    private int listenPort = 7777; // You can change this port number


    private void Start()
    {
        udpThread = new Thread(new ThreadStart(ThreadMethod))
        {
            IsBackground = true
        };
        udpThread.Start();

    }

    private void extractData(string str)
    {
        Vector3[] positions;
        string[] lines = str.Split('\n');
        bool hasLeftHandData = false;
        bool hasRightHandData = false;
        bool hasPoseData = false;
        foreach (string l in lines)
        {
            try
            {
                string[] s = l.Split('|');
                if (s.Length < 5) continue;
                int i;
                float multiplier;
                bool isPoseData = false;
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
                    break;
                }
                if (!int.TryParse(s[1], out i) || i < 0 || isPoseData ? i >= POSE_LANDMARK_COUNT : i >= HAND_LANDMARK_COUNT)
                {
                    Debug.LogWarning("Invalid landmark index");
                    break;
                }
                positions[i] = new Vector3(float.Parse(s[2]), float.Parse(s[3]), float.Parse(s[4])) * multiplier;
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
    }

    private void ThreadMethod()
    {
        udpClient = new UdpClient(listenPort);
        while (true)
        {
            try
            {
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);
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

    void OnApplicationQuit()
    {
        if (udpThread != null && udpThread.IsAlive)
        {
            udpThread.Abort();
        }

        if (udpClient != null)
        {
            udpClient.Close();
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
        float angle = Vector3.Angle(leftShoulderToRightShoulder, leftElbowToWrist);
        return angle;
    }
}
