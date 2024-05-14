using UnityEngine;

public class LineConnector : MonoBehaviour
{
    public Transform startPoint; // Assign this to the string attaching point of well_0
    public Transform endPoint;   // Assign this to the string attaching point of bucket_0
    private LineRenderer lineRenderer;

    void Start()
    {
        lineRenderer = GetComponent<LineRenderer>();
        // // // Set the color here
        // // lineRenderer.startColor = ColorUtility.TryParseHtmlString("#ED801C", out Color newCol) ? newCol : Color.white;
        // // lineRenderer.endColor = lineRenderer.startColor;
        // // Optionally set the width of the line
        // lineRenderer.startWidth = 0.12f;
        // lineRenderer.endWidth = 0.12f;
        
        // // Set rounded caps
        // lineRenderer.numCapVertices = 10; // Increase the number of vertices for a smoother round cap
        // lineRenderer.startCap = LineCap.Round;
        // lineRenderer.endCap = LineCap.Round;

        Transform bucket = endPoint.parent;

        // Calculate the difference in x-coordinates
        float xDifference = startPoint.position.x - endPoint.position.x;

        // Move bucket_0 to match the absolute x position of startPoint
        bucket.position = new Vector3(bucket.position.x + xDifference, bucket.position.y, bucket.position.z);

        // Update the positions of the line to match the moving objects
        lineRenderer.SetPosition(0, startPoint.position);
        lineRenderer.SetPosition(1, endPoint.position);
    }

    void Update()
    {
        // Update the positions of the line to match the moving objects
        lineRenderer.SetPosition(0, startPoint.position);
        lineRenderer.SetPosition(1, endPoint.position);
    }
}
