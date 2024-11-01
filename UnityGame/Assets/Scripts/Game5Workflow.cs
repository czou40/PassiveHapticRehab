using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;

public class Game5Workflow : MonoBehaviour
{
    // Start is called before the first frame update
    private int NumClenching; // Currently unused
    private float Angle;

    private float MaxAngle = -99999;
    private float MinAngle = 99999;
    // //minimum and maximum angle needed to reach to increment score
    private float MinAngleThreshold = 100.0f;
    private float MaxAngleThreshold = 170.0f;
    private int PreGameCountdown = 3;
    private int InstructionCountdown = 5;
    private int InstructionCountdownFirstTime = 10;
    private int TimerDuration = 8;
    private bool MinAngleExceeded = false;
    private bool MaxAngleExceeded = false;
    private HandMovementControl HandMovementControl;

    // private ArrayList MinAngles = new ArrayList();
    // private ArrayList MaxAngles = new ArrayList();

    private Game5Score Score;

    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private PoseVisibilityWarner PoseVisibilityWarner;
    private RoundResultShower RoundResultShower;
    private Timer Timer;
    private GameStage CurrentStage = GameStage.PRE_GAME;

    private enum GameStage
    {
        PRE_GAME,
        UNFURL_INSTRUCTION,
        UNFURL_GAME,

        CLENCH_INSTRUCTION,

        CLENCH_GAME,

        ROUND_RESULT,

        FINISHED
    }

    void Start()
    {
        NumClenching = 0;
        CurrentStage = GameStage.PRE_GAME;
        Score = new Game5Score();
        Score.MarkStartTime();
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        // game 3 requires hand in position. TODO: change for game 5
        PoseVisibilityWarner = GetComponent<PoseVisibilityWarner>();
        RoundResultShower = GetComponent<RoundResultShower>();
        HandMovementControl = GetComponent<HandMovementControl>();
        Timer = GetComponent<Timer>();
        initializeCurrentStage();
    }

    // Update is called once per frame
    void Update()
    {
        checkScore();
        if (MaxAngleExceeded && MinAngleExceeded)
        {
            //condition reached, increment score
            NumClenching += 1;
            //reset the exceed flags
            MaxAngleExceeded = false;
            MinAngleExceeded = false;
        }
    }

    void checkScore()
    {
        if (DataReceiver.isUpperBodyVisible)
        {
            Angle = DataReceiver.getLeftAverageFingerExtensionAngle();

            if (Angle > MaxAngle && CurrentStage == GameStage.UNFURL_GAME)
            {
                MaxAngle = Angle;
            }

            if (Angle < MinAngle && CurrentStage == GameStage.CLENCH_GAME)
            {
                MinAngle = Angle;
            }

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


    public void displayScore()
    {
        GameManager.Instance.sendCompoundScore(Score);
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
            case GameStage.PRE_GAME:
                CurrentStage = GameStage.UNFURL_INSTRUCTION;
                break;
            case GameStage.UNFURL_INSTRUCTION:
                CurrentStage = GameStage.UNFURL_GAME;
                break;
            case GameStage.UNFURL_GAME:
                CurrentStage = GameStage.CLENCH_INSTRUCTION;
                break;
            case GameStage.CLENCH_INSTRUCTION:
                CurrentStage = GameStage.CLENCH_GAME;
                break;
            case GameStage.CLENCH_GAME:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
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

    public void initializeCurrentStage()
    {
        switch (CurrentStage)
        {
            case GameStage.PRE_GAME:
                Debug.Log("Pre-game Start");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                resetScores();
                GameStepInstructionShower.SetInstructionText("Attempt " + (CurrentAttempt + 1) + " out of " + MaxAttempts + ". Get ready to start the game!");
                GameStepInstructionShower.ShowInstruction();
                RoundResultShower.Hide();
                GameStepInstructionShower.StartCountdown(PreGameCountdown);
                HandMovementControl.HideInstruction();
                Debug.Log("Pre-game End");
                break;
            case GameStage.UNFURL_INSTRUCTION:
                Debug.Log("UNFURL_INSTRUCTION");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("First, you need to unfurl your fingers to the maximum to harvest more fruits. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(0);
                break;
            case GameStage.UNFURL_GAME:
                Debug.Log("UNFURL_GAME");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                HandMovementControl.ShowInstruction1();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.CLENCH_INSTRUCTION:
                Debug.Log("CLENCH_INSTRUCTION");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("Now, you need to clench your fingers tightly to collect more fruits. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(1);
                HandMovementControl.HideInstruction();
                break;
            case GameStage.CLENCH_GAME:
                Debug.Log("CLENCH_GAME");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                HandMovementControl.ShowInstruction2();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.ROUND_RESULT:
                Debug.Log("ROUND_RESULT");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(Score.GetResultForRound());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
                RoundResultShower.Show();
                HandMovementControl.HideInstruction();
                break;
            case GameStage.FINISHED:
                Debug.Log("FINISHED");
                RoundResultShower.Hide();
                HandMovementControl.HideInstruction();
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
        NumClenching = 0;
        Angle = 0;
        MaxAngle = -99999;
        MinAngle = 99999;
        MaxAngleExceeded = false;
        MinAngleExceeded = false;
    }

    public void onVisibilityEndured()
    {
        switch (CurrentStage)
        {
            case GameStage.UNFURL_INSTRUCTION:
                Debug.Log("RESETTING COUNTDOWN - UNFURL");
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            case GameStage.CLENCH_INSTRUCTION:
                Debug.Log("RESETTING COUNTDOWN - CLENCH");
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            default:
                //do nothing
                break;
        }
    }

    public void onGrabFruit()
    {

    }

    public void onCheatActivated()
    {
        while (CurrentAttempt < MaxAttempts)
        {
            CurrentAttempt += 1;
            Score.AddRound(0f, 180f);
        }
        Score.MarkEndTime();
        CurrentStage = GameStage.FINISHED;
        initializeCurrentStage();
    }
}
