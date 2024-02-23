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


public class NewHandRenderer : MonoBehaviour
{

    private Vector3[] leftHandPositions = new Vector3[LANDMARK_COUNT];

    private Vector3[] rightHandPositions = new Vector3[LANDMARK_COUNT];

    private bool hasLeftHandData = false;

    private bool hasRightHandData = false;

    public float sampleThreshold = 0.25f; // how many seconds of data should be averaged to produce a single pose of the hand.

    private float lastSampleTime = -1f;

    [SerializeField] private GameObject[] leftHandJoints;
    [SerializeField] private GameObject[] rightHandJoints;

    [SerializeField] private GameObject[] leftHandLines;

    private LineRenderer[] leftHandLineRenderers;

    [SerializeField] private GameObject[] rightHandLines;

    private LineRenderer[] rightHandLineRenderers;

    private GameObject leftHand;

    private GameObject rightHand;

    [SerializeField] private bool linesEnabled = false;

    public float multiplier = 10f;
    const int LANDMARK_COUNT = 21;
    public enum Landmark
    {
        Wrist = 0,
        Thumb1 = 1,
        Thumb2 = 2,
        Thumb3 = 3,
        Thumb4 = 4,
        Index1 = 5,
        Index2 = 6,
        Index3 = 7,
        Index4 = 8,
        Middle1 = 9,
        Middle2 = 10,
        Middle3 = 11,
        Middle4 = 12,
        Ring1 = 13,
        Ring2 = 14,
        Ring3 = 15,
        Ring4 = 16,
        Pinky1 = 17,
        Pinky2 = 18,
        Pinky3 = 19,
        Pinky4 = 20
    }

    private void Start()
    {

        leftHand = leftHandJoints[0].transform.parent.gameObject;
        rightHand = rightHandJoints[0].transform.parent.gameObject;

        leftHandLineRenderers = new LineRenderer[leftHandLines.Length];
        rightHandLineRenderers = new LineRenderer[rightHandLines.Length];
        for (int i = 0; i < leftHandLines.Length; i++)
        {
            leftHandLines[i].SetActive(linesEnabled);
            rightHandLines[i].SetActive(linesEnabled);
            leftHandLineRenderers[i] = leftHandLines[i].GetComponent<LineRenderer>();
            rightHandLineRenderers[i] = rightHandLines[i].GetComponent<LineRenderer>();
            leftHandLineRenderers[i].useWorldSpace = false;
            rightHandLineRenderers[i].useWorldSpace = false;
            leftHandLineRenderers[i].positionCount = 5;
            rightHandLineRenderers[i].positionCount = 5;
        }

        udpThread = new Thread(new ThreadStart(ThreadMethod))
        {
            IsBackground = true
        };
        udpThread.Start();

    }

    private void Update()
    {
        UpdateHand();
    }

    private void extractData(string str)
    {
        Vector3[] positions;
        string[] lines = str.Split('\n');
        bool hasLeftHandData = false;
        bool hasRightHandData = false;
        foreach (string l in lines)
        {
            try
            {
                string[] s = l.Split('|');
                if (s.Length < 5) continue;
                int i;
                if (s[0] == "Left")
                {
                    positions = leftHandPositions;
                    hasLeftHandData = true;
                }
                else if (s[0] == "Right")
                {
                    positions = rightHandPositions;
                    hasRightHandData = true;
                }
                else
                {
                    Debug.LogWarning("Invalid hand data");
                    break;
                }
                if (!int.TryParse(s[1], out i) || i < 0 || i >= LANDMARK_COUNT)
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
            }
        }
        this.hasLeftHandData = hasLeftHandData;
        this.hasRightHandData = hasRightHandData;
    }

    private void UpdateHand()
    {
        if (Time.timeSinceLevelLoad - lastSampleTime >= sampleThreshold)
        {
            leftHand.SetActive(hasLeftHandData);
            rightHand.SetActive(hasRightHandData);
            updateJoints();
            if (linesEnabled) {
                UpdateLines();
            }
            lastSampleTime = Time.timeSinceLevelLoad;
        }

    }

    private void updateJoints()
    {
        for (int i = 0; i < LANDMARK_COUNT; ++i)
        {
            if (hasLeftHandData)
            {
                leftHandJoints[i].transform.localPosition = leftHandPositions[i];
            }
            if (hasRightHandData)
            {
                rightHandJoints[i].transform.localPosition = rightHandPositions[i];
            }
        }
    }

    private void UpdateLines()
    {
        if (hasLeftHandData)
        {
            leftHandLineRenderers[0].SetPosition(0, leftHandPositions[(int)Landmark.Wrist]);
            leftHandLineRenderers[0].SetPosition(1, leftHandPositions[(int)Landmark.Thumb1]);
            leftHandLineRenderers[0].SetPosition(2, leftHandPositions[(int)Landmark.Thumb2]);
            leftHandLineRenderers[0].SetPosition(3, leftHandPositions[(int)Landmark.Thumb3]);
            leftHandLineRenderers[0].SetPosition(4, leftHandPositions[(int)Landmark.Thumb4]);

            leftHandLineRenderers[1].SetPosition(0, leftHandPositions[(int)Landmark.Wrist]);
            leftHandLineRenderers[1].SetPosition(1, leftHandPositions[(int)Landmark.Index1]);
            leftHandLineRenderers[1].SetPosition(2, leftHandPositions[(int)Landmark.Index2]);
            leftHandLineRenderers[1].SetPosition(3, leftHandPositions[(int)Landmark.Index3]);
            leftHandLineRenderers[1].SetPosition(4, leftHandPositions[(int)Landmark.Index4]);

            leftHandLineRenderers[2].SetPosition(0, leftHandPositions[(int)Landmark.Wrist]);
            leftHandLineRenderers[2].SetPosition(1, leftHandPositions[(int)Landmark.Middle1]);
            leftHandLineRenderers[2].SetPosition(2, leftHandPositions[(int)Landmark.Middle2]);
            leftHandLineRenderers[2].SetPosition(3, leftHandPositions[(int)Landmark.Middle3]);
            leftHandLineRenderers[2].SetPosition(4, leftHandPositions[(int)Landmark.Middle4]);

            leftHandLineRenderers[3].SetPosition(0, leftHandPositions[(int)Landmark.Wrist]);
            leftHandLineRenderers[3].SetPosition(1, leftHandPositions[(int)Landmark.Ring1]);
            leftHandLineRenderers[3].SetPosition(2, leftHandPositions[(int)Landmark.Ring2]);
            leftHandLineRenderers[3].SetPosition(3, leftHandPositions[(int)Landmark.Ring3]);
            leftHandLineRenderers[3].SetPosition(4, leftHandPositions[(int)Landmark.Ring4]);

            leftHandLineRenderers[4].SetPosition(0, leftHandPositions[(int)Landmark.Wrist]);
            leftHandLineRenderers[4].SetPosition(1, leftHandPositions[(int)Landmark.Pinky1]);
            leftHandLineRenderers[4].SetPosition(2, leftHandPositions[(int)Landmark.Pinky2]);
            leftHandLineRenderers[4].SetPosition(3, leftHandPositions[(int)Landmark.Pinky3]);
            leftHandLineRenderers[4].SetPosition(4, leftHandPositions[(int)Landmark.Pinky4]);
        }

        if (hasRightHandData)
        {
            rightHandLineRenderers[0].SetPosition(0, rightHandPositions[(int)Landmark.Wrist]);
            rightHandLineRenderers[0].SetPosition(1, rightHandPositions[(int)Landmark.Thumb1]);
            rightHandLineRenderers[0].SetPosition(2, rightHandPositions[(int)Landmark.Thumb2]);
            rightHandLineRenderers[0].SetPosition(3, rightHandPositions[(int)Landmark.Thumb3]);
            rightHandLineRenderers[0].SetPosition(4, rightHandPositions[(int)Landmark.Thumb4]);

            rightHandLineRenderers[1].SetPosition(0, rightHandPositions[(int)Landmark.Wrist]);
            rightHandLineRenderers[1].SetPosition(1, rightHandPositions[(int)Landmark.Index1]);
            rightHandLineRenderers[1].SetPosition(2, rightHandPositions[(int)Landmark.Index2]);
            rightHandLineRenderers[1].SetPosition(3, rightHandPositions[(int)Landmark.Index3]);
            rightHandLineRenderers[1].SetPosition(4, rightHandPositions[(int)Landmark.Index4]);

            rightHandLineRenderers[2].SetPosition(0, rightHandPositions[(int)Landmark.Wrist]);
            rightHandLineRenderers[2].SetPosition(1, rightHandPositions[(int)Landmark.Middle1]);
            rightHandLineRenderers[2].SetPosition(2, rightHandPositions[(int)Landmark.Middle2]);
            rightHandLineRenderers[2].SetPosition(3, rightHandPositions[(int)Landmark.Middle3]);
            rightHandLineRenderers[2].SetPosition(4, rightHandPositions[(int)Landmark.Middle4]);

            rightHandLineRenderers[3].SetPosition(0, rightHandPositions[(int)Landmark.Wrist]);
            rightHandLineRenderers[3].SetPosition(1, rightHandPositions[(int)Landmark.Ring1]);
            rightHandLineRenderers[3].SetPosition(2, rightHandPositions[(int)Landmark.Ring2]);
            rightHandLineRenderers[3].SetPosition(3, rightHandPositions[(int)Landmark.Ring3]);
            rightHandLineRenderers[3].SetPosition(4, rightHandPositions[(int)Landmark.Ring4]);

            rightHandLineRenderers[4].SetPosition(0, rightHandPositions[(int)Landmark.Wrist]);
            rightHandLineRenderers[4].SetPosition(1, rightHandPositions[(int)Landmark.Pinky1]);
            rightHandLineRenderers[4].SetPosition(2, rightHandPositions[(int)Landmark.Pinky2]);
            rightHandLineRenderers[4].SetPosition(3, rightHandPositions[(int)Landmark.Pinky3]);
            rightHandLineRenderers[4].SetPosition(4, rightHandPositions[(int)Landmark.Pinky4]);

        }
    }

    private Thread udpThread;
    private UdpClient udpClient;
    private int listenPort = 7777; // You can change this port number


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
}
