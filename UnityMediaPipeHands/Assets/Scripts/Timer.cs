using UnityEngine;
using TMPro;
using System;
using UnityEngine.UI;


public class Timer : MonoBehaviour
{
    public float TimeLeft;
    public bool TimerOn = false;
    public TMP_Text timerText;
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
            TimerOn = false;
            TimeLeft = 0;

            HoeController hoeController = GameObject.FindObjectOfType<HoeController>();
            if (hoeController != null)
            {
                hoeController.EndGame();
            }
            else
            {
                Debug.LogError("HoeController component not found.");
            }
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