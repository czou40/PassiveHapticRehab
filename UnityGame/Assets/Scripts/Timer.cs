using UnityEngine;
using TMPro;
using System;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using static Game3Workflow;

public class Timer : MonoBehaviour
{
    private float TotalTime;
    private float RemainingTime;
    public bool TimerOn = false;
    public TMP_Text timerText;

    public Game3Workflow Game3Workflow;


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
                updateTimer(RemainingTime);
            }
            else
            {
                Debug.Log("Time is up");
                RemainingTime = 0;
                TimerOn = false;

                Game3Workflow.moveToNextStage();
                
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
        if (totalTime > 0) {
            TotalTime = totalTime;
        }
        RemainingTime = TotalTime;
        TimerOn = true;
    }
    
}