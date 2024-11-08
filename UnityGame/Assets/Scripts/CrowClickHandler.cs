using UnityEngine;

public class CrowClickHandler : MonoBehaviour
{
    private void OnMouseDown()
    {
        if (Game4Workflow.Instance != null)
        {
            Game4Workflow.Instance.OnCrowClicked();
        }
    }
}