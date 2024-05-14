using UnityEngine;

public class HorizontalLineDrawer : MonoBehaviour
{
    public Material dashedLineMaterial;
    public float lineWidth = 0.1f;
    void Start()
    {
        // Create a new GameObject for the dashed line
        GameObject dashedLine = new GameObject("DashedLine");
        LineRenderer lineRenderer = dashedLine.AddComponent<LineRenderer>();

        // Set the material
        lineRenderer.material = dashedLineMaterial;

        // Configure the LineRenderer
        lineRenderer.positionCount = 2;
        lineRenderer.startWidth = lineWidth;
        lineRenderer.endWidth = lineWidth;
        lineRenderer.numCapVertices = 0;
        lineRenderer.numCornerVertices = 0;
        lineRenderer.textureMode = LineTextureMode.Tile;
        lineRenderer.alignment = LineAlignment.TransformZ;
        lineRenderer.sortingLayerName = "UI";

        // Set the positions
        Vector3 groundPosition = transform.position;
        float screenAspect = (float)Screen.width / (float)Screen.height;
        float camHeight = Camera.main.orthographicSize * 2;
        float camWidth = camHeight * screenAspect;

        // The line will cross the whole screen width
        lineRenderer.SetPosition(0, new Vector3(groundPosition.x - camWidth, groundPosition.y, -0.01f));
        lineRenderer.SetPosition(1, new Vector3(groundPosition.x + camWidth, groundPosition.y, -0.01f));
    
    }
}
