using UnityEngine;

public class ArmMovement : MonoBehaviour
{
    [Header("Angles (Z rotation)")]
    public float downAngle = 80f;  // arm extended / grabbing carrot
    public float upAngle = 20f;    // arm bent / flexed
    public float speed = 5f;

    [Header("Elbow Angle Threshold")]
    public float elbowStraightThreshold = 150f;  // Above this = straight, below = bent
    public float elbowDistanceThreshold = 0.30f; // set after measuring
    public float hysteresis = 0.05f;

    private DataReceiver dataReceiver;
    private Quaternion targetRot;
    private bool isExtended = true; // current state
    
    [Header("Debug")]
    public bool snapToTargetForDebug = false; // when true, set rotation instantly (no Lerp)
    public bool logOnlyOnChange = true; // reduce spam
    private float lastLoggedTargetAngle = float.NaN;

    void Start()
    {
        dataReceiver = GameManager.Instance.DataReceiver;
        transform.localRotation = Quaternion.Euler(0f, 0f, downAngle);
        targetRot = transform.localRotation;
    }

    void Update()
    {
        if (dataReceiver != null && !GameManager.Instance.gamePaused && dataReceiver.isUpperBodyVisible)
        {
            // Get elbow angle from pose points
            Vector3 shoulder = dataReceiver.PosePositions[11]; // Left shoulder
            Vector3 elbow = dataReceiver.PosePositions[13];   // Left elbow
            Vector3 wrist = dataReceiver.PosePositions[15];   // Left wrist

            // Calculate distances
            float shoulderToElbow = Vector3.Distance(shoulder, elbow);
            float elbowToWrist = Vector3.Distance(elbow, wrist);
            float wristToShoulderDistance = Vector3.Distance(wrist, shoulder);

            // Debug logs: positions and distances (useful to tune thresholds)
            Debug.Log($"Positions -> Shoulder: {shoulder}, Elbow: {elbow}, Wrist: {wrist}");
            Debug.Log($"Distances -> shoulder->elbow: {shoulderToElbow:F3}, elbow->wrist: {elbowToWrist:F3}, wrist->shoulder: {wristToShoulderDistance:F3}");

            // Use shoulder->elbow distance: if > 0.2 then down (bent), else up (straight)
            float targetAngle = (shoulderToElbow > 0.2f) ? downAngle : upAngle;
            targetRot = Quaternion.Euler(0, 0, targetAngle);

            // Log target/current rotation and quaternion difference to debug why visuals might not move
            float currentZ = transform.localRotation.eulerAngles.z;
            float quatDiff = Quaternion.Angle(transform.localRotation, targetRot);
            if (!logOnlyOnChange || !Mathf.Approximately(lastLoggedTargetAngle, targetAngle))
            {
                Debug.Log($"ArmMovement DEBUG -> currentZ: {currentZ:F2}, targetAngle: {targetAngle:F2}, quatAngleDiff: {quatDiff:F3}");
                lastLoggedTargetAngle = targetAngle;
            }
        }

        // Smoothly rotate toward target (or snap for debugging)
        if (snapToTargetForDebug)
        {
            transform.localRotation = targetRot;
        }
        else
        {
            transform.localRotation = Quaternion.Lerp(
                transform.localRotation,
                targetRot,
                Time.deltaTime * speed
            );
        }
    }
}
