using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;

public class ScoreControl1 : MonoBehaviour
{
    // Start is called before the first frame update
    private int Score;
    private float Angle;
    //minimum and maximum angle needed to reach to increment score
    private float MinAngleThreshold = 50;
    private float MaxAngleThreshold = 100;
    private bool MinAngleExceeded = false;
    private bool MaxAngleExceeded = false;

    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private GameStage CurrentStage = GameStage.SHOULDER_UP_INSTRUCTION;

    private enum GameStage
    {
        PRE_GAME,
        SHOULDER_UP_INSTRUCTION,
        SHOULDER_UP_GAME,

        SHOULDER_DOWN_INSTRUCTION,

        SHOULDER_DOWN_GAME,

        FINISHED
    }

    void Start()
    {
        Score = 0;
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
    }

    // Update is called once per frame
    void Update()
    {
        while (CurrentAttempt < MaxAttempts)
        {
            checkScore();
            Debug.Log("Number of tries: " + CurrentAttempt);
            if (MaxAngleExceeded && MinAngleExceeded)
            {
                //condition reached, increment score
                Score += 5;
                //reset the exceed flags
                MaxAngleExceeded = false;
                MinAngleExceeded = false;
                break;
            }
            CurrentAttempt++;
        }
        Debug.Log("Score: " + Score);
    }

    void checkScore()
    {
        if (DataReceiver.isUpperBodyVisible)
        {
            Angle = DataReceiver.getLeftShoulderExtensionAngle();
            MinAngleThreshold = getGameMinTarget();

            if (Angle > MaxAngleThreshold)
            {
                MaxAngleExceeded = true;
            }
            else if (Angle < MinAngleThreshold)
            {
                MinAngleExceeded = true;
            }
        }
    }

    float getGameMinTarget()
    {
        return MinAngleThreshold;
    }

    public void displayScore()
    {
        GameManager.Instance.DisplayScore(Game.Game1, Score);
    }

    public void onVisibilityLost()
    {

    }

    public void onVisibilityGained()
    {

    }

    public void moveToNextStage()
    {
        Debug.Log("Prev Stage: " + CurrentStage);
        switch (CurrentStage)
        {
            case GameStage.SHOULDER_UP_INSTRUCTION:
                CurrentStage = GameStage.SHOULDER_UP_GAME;
                break;
            case GameStage.SHOULDER_UP_GAME:
                CurrentStage = GameStage.SHOULDER_DOWN_INSTRUCTION;
                break;
            case GameStage.SHOULDER_DOWN_INSTRUCTION:
                CurrentStage = GameStage.SHOULDER_DOWN_GAME;
                break;
            case GameStage.SHOULDER_DOWN_GAME:
                CurrentAttempt += 1;
                if (CurrentAttempt < MaxAttempts)
                {
                    CurrentStage = GameStage.SHOULDER_UP_INSTRUCTION;
                }
                else
                {
                    CurrentStage = GameStage.FINISHED;
                }
                break;
            default:
                //do nothing
                break;
        }
        Debug.Log("Next Stage: " + CurrentStage);

        initializeCurrentStage();
    }

    public void initializeCurrentStage()
    {
        switch (CurrentStage)
        {
            case GameStage.SHOULDER_UP_INSTRUCTION:
                GameStepInstructionShower.SetInstructionText("First, you need to flex your shoulder as high as you can to gather more power. Ready?");
                GameStepInstructionShower.ShowInstruction();
                break;
            case GameStage.SHOULDER_DOWN_INSTRUCTION:
                GameStepInstructionShower.SetInstructionText("Great! Now you can extend your shoulder and push back your arm to harvest!");
                GameStepInstructionShower.ShowInstruction();
                break;
            default:
                GameStepInstructionShower.HideInstruction();
                break;
        }
    }

    public void onVisibilityEndured()
    {
        switch (CurrentStage)
        {
            case GameStage.SHOULDER_UP_INSTRUCTION:
                GameStepInstructionShower.StartCountdown();
                break;
            case GameStage.SHOULDER_DOWN_INSTRUCTION:
                GameStepInstructionShower.StartCountdown();
                break;
            default:
                //do nothing
                break;
        }
    }
}
