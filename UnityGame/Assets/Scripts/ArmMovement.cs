using UnityEngine;

public class ArmMovement : MonoBehaviour
{
    [Header("Angles (Z rotation)")]
    public float downAngle = 80f;  // arm extended / grabbing carrot
    public float upAngle = 20f;    // arm bent / flexed
    public float speed = 5f;

    [Header("Elbow Angle Threshold")]
    public float elbowStraightThreshold = 150f;  // Above this = straight, below = bent

    private DataReceiver dataReceiver;
    private Quaternion targetRot;

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

            // Calculate distance from wrist to shoulder
            float wristToShoulderDistance = Vector3.Distance(wrist, shoulder);
            
            Debug.Log($"Wrist to shoulder distance: {wristToShoulderDistance}");
            
            // If wrist is closer to shoulder (arm bent), go up. If further (arm extended), go down
            float targetAngle = (wristToShoulderDistance < 0.3f) ? upAngle : downAngle;
            targetRot = Quaternion.Euler(0, 0, targetAngle);
        }

        // Smoothly rotate toward target
        transform.localRotation = Quaternion.Lerp(
            transform.localRotation,
            targetRot,
            Time.deltaTime * speed
        );
    }
}
