using UnityEngine;

public class IntroductionButton : MonoBehaviour
{
    private FingerToNoseWorkflow workflow;

    void Start()
    {
        workflow = FindObjectOfType<FingerToNoseWorkflow>();
    }

    void Update()
    {
        if (Input.GetMouseButtonDown(0))
        {
            Vector2 mousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
            if (IsMouseOverButton(mousePos))
            {
                workflow.moveToNextStage();
            }
        }
    }

    private bool IsMouseOverButton(Vector2 mousePos)
    {
        Collider2D collider = GetComponent<Collider2D>();
        return collider != null && collider.OverlapPoint(mousePos);
    }
}
