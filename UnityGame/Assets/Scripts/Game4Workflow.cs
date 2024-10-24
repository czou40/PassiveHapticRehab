using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;

// Basically copy this file and use our flow
// https://github.com/czou40/PassiveHapticRehab/blob/master/UnityGame/Assets/Scripts/Game1Workflow.cs
public class Game4Workflow : MonoBehaviour
{
    private Game4Score Score;
    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    private int TimerDuration = 30; // length of each round

    private int CrowClicksThisRound = 0;

    private int PreGameCountdown = 3;  // seconds before the game begins
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private Timer Timer;
    private GameStage CurrentStage;
    private RoundResultShower RoundResultShower;

    private enum GameStage
    {
        PRE_GAME,
        FINGER_TO_NOSE_INSTRUCTION,
        FINGER_TO_NOSE_GAME,

        ROUND_RESULT,

        FINISHED
    }

    void Start()
    {
        // PRE_GAME - This stage will give an initial few seconds before the game begins, and game timer starts counting down from 30 seconds
        CurrentStage = GameStage.PRE_GAME;
        Score = new Game4Score();
        Score.MarkStartTime();
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        RoundResultShower = GetComponent<RoundResultShower>();
        Timer = GetComponent<Timer>();
        initializeCurrentStage();
    }

    public void initializeCurrentStage()
    {
        switch (CurrentStage)
        {
            case GameStage.PRE_GAME:
                GameManager.Instance.PauseGame();
                resetScores();
                GameStepInstructionShower.SetInstructionText("Attempt " + (CurrentAttempt + 1) + " out of " + MaxAttempts + ". Get ready to start the game!");
                GameStepInstructionShower.ShowInstruction();
                RoundResultShower.Hide();
                GameStepInstructionShower.StartCountdown(PreGameCountdown);
                break;
            case GameStage.FINGER_TO_NOSE_INSTRUCTION:
                GameManager.Instance.PauseGame();
                GameStepInstructionShower.SetInstructionText("First, you need to get your face within the camera's view. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(0);
                break;
            case GameStage.FINGER_TO_NOSE_GAME:
                GameManager.Instance.PauseGame();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                Timer.StartTimer(TimerDuration);
                GameManager.Instance.ResumeGame(); //Added - Yash T
                GameManager.Instance.StartGameplay(); //Added - Yash T
                break;
            case GameStage.ROUND_RESULT:
                GameManager.Instance.PauseGame();
                GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(Score.GetResultForRound());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
                RoundResultShower.Show();
                break;
            case GameStage.FINISHED:
                RoundResultShower.Hide();
                Debug.Log("Game Finished");
                displayScore();
                break;
            default:
                GameStepInstructionShower.HideInstruction();
                break;
        }
    }

    private void resetScores()
    {
        CrowClicksThisRound = 0;
    }

    public void displayScore()
    {
        GameManager.Instance.sendCompoundScore(Score);
    }

    public void IncrementCrowClicks()
    {
        CrowClicksThisRound++;
    }

    public void moveToNextStage()
    {
        Debug.Log("Prev Stage: " + CurrentStage);
        switch (CurrentStage)
        {
            case GameStage.PRE_GAME:
                CurrentStage = GameStage.FINGER_TO_NOSE_INSTRUCTION;
                break;
            case GameStage.FINGER_TO_NOSE_GAME:
                CurrentAttempt += 1;
                Score.AddRound(CrowClicksThisRound);
                CurrentStage = GameStage.ROUND_RESULT;
                break;
            case GameStage.ROUND_RESULT:
                if (CurrentAttempt < MaxAttempts)
                {
                    CurrentStage = GameStage.PRE_GAME;
                }
                else
                {
                    Score.MarkEndTime();
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

}
