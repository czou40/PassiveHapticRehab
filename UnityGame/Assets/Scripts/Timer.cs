using UnityEngine;
using TMPro;
using System;
using UnityEngine.UI;
using UnityEngine.SceneManagement;

public class Timer : MonoBehaviour
{
    private float TotalTime;
    private float RemainingTime;
    public bool TimerOn = false;
    public TMP_Text timerText;

    // Inspector-assignable workflow component. Timer will call moveToNextStage() on this component when time runs out.
    public MonoBehaviour GameWorkflow;

    void Start()
    {
        RemainingTime = TotalTime;
        TimerOn = false;
    }

    private void Update()
    {
        if (TimerOn)
        {
            if (RemainingTime > 0)
            {
                RemainingTime -= Time.deltaTime;
                updateTimer(RemainingTime);
            }
            else
            {
                Debug.Log("Time is up");
                RemainingTime = 0;
                TimerOn = false;

                if (GameWorkflow != null)
                {
                    // call moveToNextStage on the assigned workflow component (expects parameterless method)
                    GameWorkflow.Invoke("moveToNextStage", 0f);
                }
                else
                {
                    Debug.LogWarning("Timer: GameWorkflow not assigned (cannot call moveToNextStage)");
                }
            }
        }
    }

    private void updateTimer(float currentTime)
    {
        currentTime += 1;

        float minutes = Mathf.FloorToInt(currentTime / 60);
        float seconds = Mathf.FloorToInt(currentTime % 60);

        timerText.text = string.Format("{0:00} : {1:00}", minutes, seconds);
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