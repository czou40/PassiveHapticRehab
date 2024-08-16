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
