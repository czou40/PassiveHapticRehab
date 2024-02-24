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


public class HandVisualizer : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;
    [SerializeField] private GameObject[] leftHandJoints;
    [SerializeField] private GameObject[] rightHandJoints;

    [SerializeField] private GameObject[] leftHandLines;

    private LineRenderer[] leftHandLineRenderers;

    [SerializeField] private GameObject[] rightHandLines;

    private LineRenderer[] rightHandLineRenderers;

    private GameObject leftHand;

    private GameObject rightHand;

    [SerializeField] private bool linesEnabled = false;

    const float SAMPLE_THRESHOLD = 0f; // how many seconds of data should be averaged to produce a single pose of the hand.
    private float lastSampleTime = -1f;
    const int HAND_LANDMARK_COUNT = 21;

    public enum HandLandmark
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

    readonly Dictionary<int, HandLandmark[]> lineToLandmarkMapping = new Dictionary<int, HandLandmark[]>
    {
        { 0, new[] { HandLandmark.Wrist, HandLandmark.Thumb1, HandLandmark.Thumb2, HandLandmark.Thumb3, HandLandmark.Thumb4 } },
        { 1, new[] { HandLandmark.Wrist, HandLandmark.Index1, HandLandmark.Index2, HandLandmark.Index3, HandLandmark.Index4 } },
        { 2, new[] { HandLandmark.Wrist, HandLandmark.Middle1, HandLandmark.Middle2, HandLandmark.Middle3, HandLandmark.Middle4 } },
        { 3, new[] { HandLandmark.Wrist, HandLandmark.Ring1, HandLandmark.Ring2, HandLandmark.Ring3, HandLandmark.Ring4 } },
        { 4, new[] { HandLandmark.Wrist, HandLandmark.Pinky1, HandLandmark.Pinky2, HandLandmark.Pinky3, HandLandmark.Pinky4 } },
    };



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
            leftHandLineRenderers[i].positionCount = lineToLandmarkMapping[i].Length;
            rightHandLineRenderers[i].positionCount = lineToLandmarkMapping[i].Length;
        }
    }

    private void Update()
    {
        UpdateHand();
    }

    private void UpdateHand()
    {
        if (Time.timeSinceLevelLoad - lastSampleTime >= SAMPLE_THRESHOLD)
        {
            leftHand.SetActive(dataReceiver.HasLeftHandData);
            rightHand.SetActive(dataReceiver.HasRightHandData);
            updateJoints();
            if (linesEnabled)
            {
                UpdateLines();
            }
            lastSampleTime = Time.timeSinceLevelLoad;
        }

    }

    private void updateJoints()
    {
        for (int i = 0; i < HAND_LANDMARK_COUNT; ++i)
        {
            if (dataReceiver.HasLeftHandData)
            {
                leftHandJoints[i].transform.localPosition = dataReceiver.LeftHandPositions[i];
            }
            if (dataReceiver.HasRightHandData)
            {
                rightHandJoints[i].transform.localPosition = dataReceiver.RightHandPositions[i];
            }
        }
    }

    private void UpdateLines()
    {
        if (dataReceiver.HasLeftHandData)
        {
            foreach (var mapping in lineToLandmarkMapping)
            {
                for (int i = 0; i < mapping.Value.Length; i++)
                {
                    leftHandLineRenderers[mapping.Key].SetPosition(i, dataReceiver.LeftHandPositions[(int)mapping.Value[i]]);
                }
            }
        }
        if (dataReceiver.HasRightHandData)
        {
            foreach (var mapping in lineToLandmarkMapping)
            {
                for (int i = 0; i < mapping.Value.Length; i++)
                {
                    rightHandLineRenderers[mapping.Key].SetPosition(i, dataReceiver.RightHandPositions[(int)mapping.Value[i]]);
                }
            }
        }
    }
}
