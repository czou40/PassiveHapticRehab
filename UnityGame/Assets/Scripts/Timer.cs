using UnityEngine;
using TMPro;

public class Timer : MonoBehaviour
{
    private float TotalTime = 30f; // Set the default total time to 30 seconds
    private float RemainingTime;
    public bool TimerOn = false;
    public TMP_Text timerText;

    public Game1Workflow game1Workflow;
    public Game6Workflow game6Workflow;

    void Start()
    {
        RemainingTime = TotalTime;
        TimerOn = false;
    }

    private void Update()
    {
        if (TimerOn)
        {
            if (RemainingTime >= 0)
            {
                RemainingTime -= Time.deltaTime;
                UpdateTimer(RemainingTime);
            }
            else
            {
                Debug.Log("Time is up");
                RemainingTime = 0;
                TimerOn = false;

                if (game1Workflow != null)
                {
                    game1Workflow.moveToNextStage();
                }
                else if (game6Workflow != null)
                {
                    game6Workflow.moveToNextStage();
                }
            }
        }
    }

    private void UpdateTimer(float currentTime)
    {
        float minutes = Mathf.FloorToInt(currentTime / 60);
        float seconds = Mathf.FloorToInt(currentTime % 60);
        timerText.text = string.Format("{0:00}:{1:00}", minutes, seconds);
    }

    public void StartTimer(float totalTime = -1f)
    {
        if (totalTime > 0)
        {
            TotalTime = totalTime;
        }
        RemainingTime = TotalTime;
        TimerOn = true;
    }
}
