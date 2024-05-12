using UnityEngine;
using TMPro;
using System;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using static ScoreControl;

public class Timer : MonoBehaviour
{
    public float TimeLeft;
    public bool TimerOn = false;
    public TMP_Text timerText;

    public ScoreControl scoreControl;


    public 
    void Start()
    {
        TimerOn = true;
    }



    private void Update()
    {
        if (TimerOn)
        {
            if (TimeLeft > 0)
            {
                TimeLeft -= Time.deltaTime;
                updateTimer(TimeLeft);
            }
            else
            {
                Debug.Log("Time is up");
                TimeLeft = 0;
                TimerOn = false;

                scoreControl.displayScore();
                
            }
        }
    }
    void updateTimer(float currentTime)
    {
        currentTime += 1;

        float minutes = Mathf.FloorToInt(currentTime / 60);
        float seconds = Mathf.FloorToInt(currentTime % 60);

        timerText.text = string.Format("{0:00} : {1:00}", minutes, seconds);
    }
}