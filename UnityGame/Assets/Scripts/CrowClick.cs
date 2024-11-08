using UnityEngine;

public class CrowClick : MonoBehaviour
{
    // This method will be called when the crow is tapped
    public void OnCrowTapped()
    {
        if (Game4Workflow.Instance != null)
        {
            Debug.Log("Crow clicked internal"); // Debug statement for next stage transition
            Game4Workflow.Instance.CrowTapped(); // Notify Game4Workflow of the crow tap
        }
    }

    public void OnMouseDown()
    {
        if (Game4Workflow.Instance != null)
        {
            Game4Workflow.Instance.CrowTapped();
        }
    }


}
