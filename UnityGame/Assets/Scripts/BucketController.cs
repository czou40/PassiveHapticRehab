using UnityEngine;

public class BuckerController : MonoBehaviour
{

    private DataReceiver dataReceiver;
    [SerializeField] private GameObject bucketHighestPoint;
    [SerializeField] private GameObject bucketLowestPoint;
    // When false, the bucket will not respond to wrist angle updates.
    public bool MovementEnabled = false;


    private float lowestPointY;
    private float highestPointY;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        lowestPointY = bucketLowestPoint.transform.position.y;   
        highestPointY = bucketHighestPoint.transform.position.y;
        dataReceiver = GameManager.Instance.DataReceiver;
    }

    // Update is called once per frame
    void Update()
    {
        if (!MovementEnabled) return;

        long currentTime = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();
        if (dataReceiver.PoseDataTimeStamp > currentTime - 500 && dataReceiver.LeftHandDataTimeStamp > currentTime - 500)
        {
            float angle = dataReceiver.getLeftWristExtensionAngle();
            // Angle is between 0 to 180. If the angle is 0, the bucket is at the highest point. If the angle is 180, the bucket is at the lowest point.
            float y = Mathf.Lerp(highestPointY, lowestPointY, angle / 180f);
            Vector3 pos = transform.position;
            pos.y = y;
            transform.position = pos;

            Debug.Log("Angle: " + angle);
        }
    }
}
