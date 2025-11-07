using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using TMPro;

public class Game3Workflow : MonoBehaviour
{
    [Header("UI Panels")]
    public GameObject countdownPanel;
    public GameObject uiFlowPanel;

    [Header("Countdown Text")]
    public TMP_Text countdownText;
    public int startTime = 3;

    void Start()
    {
        // Make sure initial state is correct
        countdownPanel.SetActive(true);
        uiFlowPanel.SetActive(false);

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
