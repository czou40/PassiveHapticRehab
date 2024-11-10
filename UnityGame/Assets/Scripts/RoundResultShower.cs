using UnityEngine;
using TMPro;


public class RoundResultShower : MonoBehaviour
{
    public GameObject ResultPanel;
    public TextMeshProUGUI ResultText;
    public TextMeshProUGUI NextButtonText;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

    }

    // This method will be called when the next round is tapped
    public void OnNextRoundButtonTapped()
    {
        if (Game6Workflow.Instance != null)
        {
            Game6Workflow.Instance.moveToNextStage(); // Notify Game6Workflow of the tap
        }
    }

    public void OnMouseDown()
    {
        if (Game6Workflow.Instance != null)
        {
            Game6Workflow.Instance.moveToNextStage();
        }
        Destroy(gameObject);
    }


    public void Show()
    {
        ResultPanel.SetActive(true);
    }


    public void Hide()
    {
        ResultPanel.SetActive(false);
    }

    public void SetResultText(string result)
    {
        ResultText.text = result;
    }

    public void SetNextButtonText(string next)
    {
        NextButtonText.text = next;
    }

}
