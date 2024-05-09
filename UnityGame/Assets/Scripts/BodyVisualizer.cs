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


public class BodyVisualizer : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;
    [SerializeField] private GameObject[] landmarks;

    [SerializeField] private GameObject[] lines;

    private GameObject body;

    private LineRenderer[] lineRenderers;

    [SerializeField] private bool linesEnabled = false;

    const float SAMPLE_THRESHOLD = 0f;
    private float lastSampleTime = -1f;
    const int POSE_LANDMARK_COUNT = 33;

    public enum PoseLandmark
    {
        Nose = 0,
        LeftEyeInner = 1,
        LeftEye = 2,
        LeftEyeOuter = 3,
        RightEyeInner = 4,
        RightEye = 5,
        RightEyeOuter = 6,
        LeftEar = 7,
        RightEar = 8,
        MouthLeft = 9,
        MouthRight = 10,
        LeftShoulder = 11,
        RightShoulder = 12,
        LeftElbow = 13,
        RightElbow = 14,
        LeftWrist = 15,
        RightWrist = 16,
        LeftPinky = 17,
        RightPinky = 18,
        LeftIndex = 19,
        RightIndex = 20,
        LeftThumb = 21,
        RightThumb = 22,
        LeftHip = 23,
        RightHip = 24,
        LeftKnee = 25,
        RightKnee = 26,
        LeftAnkle = 27,
        RightAnkle = 28,
        LeftHeel = 29,
        RightHeel = 30,
        LeftFootIndex = 31,
        RightFootIndex = 32
    }
    readonly Dictionary<int, PoseLandmark[]> lineToLandmarkMapping = new Dictionary<int, PoseLandmark[]>
    {
        { 0, new[] { PoseLandmark.LeftShoulder, PoseLandmark.RightShoulder, PoseLandmark.RightHip, PoseLandmark.LeftHip, PoseLandmark.LeftShoulder } },
        { 1, new[] { PoseLandmark.LeftShoulder, PoseLandmark.LeftElbow, PoseLandmark.LeftWrist, PoseLandmark.LeftPinky, PoseLandmark.LeftIndex, PoseLandmark.LeftWrist, PoseLandmark.LeftThumb } },
        { 2, new[] { PoseLandmark.RightShoulder, PoseLandmark.RightElbow, PoseLandmark.RightWrist, PoseLandmark.RightPinky, PoseLandmark.RightIndex, PoseLandmark.RightWrist, PoseLandmark.RightThumb } },
        { 3, new[] { PoseLandmark.LeftHip, PoseLandmark.LeftKnee, PoseLandmark.LeftAnkle, PoseLandmark.LeftHeel, PoseLandmark.LeftFootIndex, PoseLandmark.LeftAnkle } },
        { 4, new[] { PoseLandmark.RightHip, PoseLandmark.RightKnee, PoseLandmark.RightAnkle, PoseLandmark.RightHeel, PoseLandmark.RightFootIndex, PoseLandmark.RightAnkle } },
    };



    private void Start()
    {
        body = lines[0].transform.parent.gameObject;
        lineRenderers = new LineRenderer[lines.Length];
        for (int i = 0; i < lines.Length; i++)
        {
            lines[i].SetActive(linesEnabled);
            lineRenderers[i] = lines[i].GetComponent<LineRenderer>();
            lineRenderers[i].useWorldSpace = false;
            lineRenderers[i].positionCount = lineToLandmarkMapping[i].Length;
        }
    }

    private void Update()
    {
        UpdatePose();
    }

    private void UpdatePose()
    {
        if (Time.timeSinceLevelLoad - lastSampleTime >= SAMPLE_THRESHOLD)
        {
            body.SetActive(dataReceiver.HasPoseData);
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
        if (dataReceiver.HasPoseData)
        {
            for (int i = 0; i < POSE_LANDMARK_COUNT; ++i)
            {
                landmarks[i].transform.localPosition = dataReceiver.PosePositions[i];
            }
        }
    }

    private void UpdateLines()
    {
        if (dataReceiver.HasPoseData)
        {
            foreach (var mapping in lineToLandmarkMapping)
            {
                for (int i = 0; i < mapping.Value.Length; i++)
                {
                    lineRenderers[mapping.Key].SetPosition(i, dataReceiver.PosePositions[(int)mapping.Value[i]]);
                }
            }
        }
    }
}
