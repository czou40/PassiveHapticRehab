using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using TMPro;

public class Game3Workflow : MonoBehaviour
{
    [Header("UI Panels")]
    public GameObject instructionsPanel;   // new
    public GameObject countdownPanel;
    public GameObject uiFlowPanel;

    [Header("Countdown Text")]
    public TMP_Text countdownText;
    public int startTime = 3;

    private bool hasStarted = false;

    void Start()
    {
        // Show instructions first
        if (instructionsPanel != null)
            instructionsPanel.SetActive(true);

        countdownPanel.SetActive(false);
        uiFlowPanel.SetActive(false);
    }

    // Hook this to the "Lets Begin" button
    public void OnBeginButtonPressed()
    {
        if (hasStarted) return;
        hasStarted = true;

        if (instructionsPanel != null)
            instructionsPanel.SetActive(false);

        countdownPanel.SetActive(true);
        StartCoroutine(StartCountdown());
    }

    IEnumerator StartCountdown()
    {
        int time = startTime;

        while (time > 0)
        {
            countdownText.text = time.ToString();
            yield return new WaitForSeconds(1f);
            time--;
        }

        countdownText.text = "";

        // Transition to UIFlow
        countdownPanel.SetActive(false);
        uiFlowPanel.SetActive(true);
    }
}
