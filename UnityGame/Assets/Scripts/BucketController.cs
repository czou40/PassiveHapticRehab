using UnityEngine;

public class BuckerController : MonoBehaviour
{

    [SerializeField] private DataReceiver dataReceiver;
    [SerializeField] private GameObject bucketHighestPoint;
    [SerializeField] private GameObject bucketLowestPoint;


    private float lowestPointY;
    private float highestPointY;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        lowestPointY = bucketLowestPoint.transform.position.y;   
        highestPointY = bucketHighestPoint.transform.position.y;
    }

    // Update is called once per frame
    void Update()
    {
        long currentTime = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();
            if (dataReceiver.PoseDataTimeStamp > currentTime - 500 && dataReceiver.LeftHandDataTimeStamp > currentTime - 500) {
            float angle = dataReceiver.getLeftWristExtensionAngle();
            // Angle is between 0 to 180. If the angle is 0, the bucket is at the highest point. If the angle is 180, the bucket is at the lowest point.
            float y = Mathf.Lerp(highestPointY, lowestPointY, angle / 180);
            Vector3 pos = transform.position;
            pos.y = y;
            transform.position = pos;
        
            Debug.Log("Angle: " + angle);
        }
    }
}
