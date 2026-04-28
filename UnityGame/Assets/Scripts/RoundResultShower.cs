using UnityEngine;
using TMPro;
using UnityEngine.Events;

public class RoundResultShower : MonoBehaviour
{
    public GameObject ResultPanel;
    public TextMeshProUGUI ResultText;
    public TextMeshProUGUI NextButtonText;
    
    // Event that gets called when the next button is clicked
    public UnityEvent onNextButtonClicked;

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

    public void OnNextButtonClicked()
    {
        onNextButtonClicked.Invoke();
    }
}
