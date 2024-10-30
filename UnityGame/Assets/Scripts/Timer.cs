using UnityEngine;
using TMPro;
using System;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using static Game1Workflow;

public class Timer : MonoBehaviour
{
    private float TotalTime;
    private float RemainingTime;
    public bool TimerOn = false;
    public TMP_Text timerText;

    public Game1Workflow Game1Workflow;

    public Game4Workflow Game4Workflow;


    public 
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
                updateTimerUI();
            }
            else
            {
                Debug.Log("Time is up");
                RemainingTime = 0;
                TimerOn = false;

                Game1Workflow.moveToNextStage();
                Game4Workflow.moveToNextStage();
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

    private void updateTimerUI() 
    {
        int minutes = Mathf.FloorToInt(RemainingTime / 60);
        int seconds = Mathf.FloorToInt(RemainingTime % 60);
        timerText.text = $"{minutes:00}:{seconds:00}";
    }

    public void StartTimer(float duration)
    {
        RemainingTime = duration;
        TimerOn = true;
    }
    
}
