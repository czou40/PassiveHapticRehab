using UnityEngine;
using TMPro;


public class PreGameInstructions : MonoBehaviour
{
    public GameObject ResultPanel;
    public TextMeshProUGUI NextButtonText;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

    }

    public void Show()
    {
        ResultPanel.SetActive(true);
    }


    public void Hide()
    {
        ResultPanel.SetActive(false);
    }

    public void SetNextButtonText(string next)
    {
        NextButtonText.text = next;
    }

}
