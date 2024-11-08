using UnityEngine;
using TMPro;
using UnityEngine.UI;

public class RoundResultShower : MonoBehaviour
{
    public GameObject ResultPanel;
    public TextMeshProUGUI ResultText;
    public Button NextRoundButton;

    public TextMeshProUGUI NextButtonText; // Not used in game 4

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
        if (Game4Workflow.Instance != null)
        {
            Game4Workflow.Instance.moveToNextStage(); // Notify Game4Workflow of the tap
        }
    }

    public void OnMouseDown()
    {
        if (Game4Workflow.Instance != null)
        {
            Game4Workflow.Instance.moveToNextStage();
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
