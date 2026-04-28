using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;

public class Game4Workflow : MonoBehaviour
{
    // Start is called before the first frame update
    private int NumWavings; // Currently unused
    private float WristAngle;

    private float MaxWristAngle = -99999;
    private float MinWristAngle = 99999;
    //minimum and maximum angle needed to reach to increment score
    private float MinAngleThreshold = 50;
    private float MaxAngleThreshold = 100;
    private int PreGameCountdown = 3;
    private int InstructionCountdown = 5;
    private int InstructionCountdownFirstTime = 10;
    private int TimerDuration = 8;
    private bool MinAngleExceeded = false;
    private bool MaxAngleExceeded = false;

    private Game1Score Score;

    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private PoseVisibilityWarner PoseVisibilityWarner;
    private RoundResultShower RoundResultShower;
    // Game4 uses the bucket controller for bucket movement; no shoulder animations needed
    private WristAnimationControl WristAnimationControl;
    private Timer Timer;
    private BuckerController BucketController;
    private GameStage CurrentStage = GameStage.PRE_GAME;

    private enum GameStage
    {
        PRE_GAME,
        WRIST_DOWN_INSTRUCTION,
        WRIST_DOWN_GAME,

        WRIST_UP_INSTRUCTION,

        WRIST_UP_GAME,

        ROUND_RESULT,

        FINISHED
    }

    void Start()
    {
        NumWavings = 0;
        CurrentStage = GameStage.PRE_GAME;
        Score = new Game1Score();
        Score.MarkStartTime();
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        // wire the instruction countdown end event to advance stages if possible
        if (GameStepInstructionShower != null)
        {
            GameStepInstructionShower.onInstructionCountdownEnd.RemoveAllListeners();
            GameStepInstructionShower.onInstructionCountdownEnd.AddListener(() => moveToNextStage());
        }
        PoseVisibilityWarner = GetComponent<PoseVisibilityWarner>();
        RoundResultShower = GetComponent<RoundResultShower>();
        // wire the round result next button to advance stages
        if (RoundResultShower != null)
        {
            RoundResultShower.onNextButtonClicked.RemoveAllListeners();
            RoundResultShower.onNextButtonClicked.AddListener(() => moveToNextStage());
        }
        WristAnimationControl = GetComponent<WristAnimationControl>();
        Timer = GetComponent<Timer>();
        // try to find the bucket controller in scene (bucket script name is BuckerController)
        BucketController = FindObjectOfType<BuckerController>();
        if (BucketController != null)
        {
            BucketController.MovementEnabled = false; // lock movement until game stages allow it
        }
        // if Timer exists, make sure it calls this workflow when it times out
        if (Timer != null)
        {
            // assign this workflow to the Timer's GameWorkflow field so Timer will invoke moveToNextStage
            Timer.GameWorkflow = this;
        }
        initializeCurrentStage();
    }

    // Update is called once per frame
    void Update()
    {
        checkScore();
        if (MaxAngleExceeded && MinAngleExceeded)
        {
            //condition reached, increment score
            NumWavings += 1;
            //reset the exceed flags
            MaxAngleExceeded = false;
            MinAngleExceeded = false;
        }
    }

    void checkScore()
    {
        if (DataReceiver.isUpperBodyVisible)
        {
            WristAngle = DataReceiver.getLeftWristExtensionAngle();

            if (WristAngle > MaxWristAngle && CurrentStage == GameStage.WRIST_UP_GAME)
            {
                MaxWristAngle = WristAngle;
            }

            if (WristAngle < MinWristAngle && CurrentStage == GameStage.WRIST_DOWN_GAME)
            {
                MinWristAngle = WristAngle;
            }

            if (WristAngle > MaxAngleThreshold)
            {
                MaxAngleExceeded = true;
            }
            else if (WristAngle < MinAngleThreshold)
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
                CurrentStage = GameStage.WRIST_DOWN_INSTRUCTION;
                break;
            case GameStage.WRIST_DOWN_INSTRUCTION:
                CurrentStage = GameStage.WRIST_DOWN_GAME;
                break;
            case GameStage.WRIST_DOWN_GAME:
                CurrentStage = GameStage.WRIST_UP_INSTRUCTION;
                break;
            case GameStage.WRIST_UP_INSTRUCTION:
                CurrentStage = GameStage.WRIST_UP_GAME;
                break;
            case GameStage.WRIST_UP_GAME:
                CurrentAttempt += 1;
                Score.AddRound(MinWristAngle, MaxWristAngle);
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
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                resetScores();
                GameStepInstructionShower.SetInstructionText("Attempt " + (CurrentAttempt + 1) + " out of " + MaxAttempts + ". Get ready to start the game!");
                GameStepInstructionShower.ShowInstruction();
                RoundResultShower.Hide();
                GameStepInstructionShower.StartCountdown(PreGameCountdown);
                if (WristAnimationControl != null) WristAnimationControl.HideAnimations();
                break;
            case GameStage.WRIST_DOWN_INSTRUCTION:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("First, you need to flex your wrist as low as you can to gather the water. Ready?");
                GameStepInstructionShower.ShowInstruction();
                // Show wrist down animation
                if (WristAnimationControl != null) WristAnimationControl.ShowWristDownAnimation();
                break;
            case GameStage.WRIST_DOWN_GAME:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                Debug.Log("Game4Workflow: entering WRIST_DOWN_GAME");
                if (Timer != null) { Debug.Log("Game4Workflow: Starting Timer for WRIST_DOWN_GAME"); Timer.StartTimer(TimerDuration); }
                if (BucketController != null) { BucketController.MovementEnabled = true; }
                if (WristAnimationControl != null) WristAnimationControl.HideAnimations();
                break;
            case GameStage.WRIST_UP_INSTRUCTION:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("Great! Now you can extend your wrist upwards to drop the water in the well! Ready?");
                GameStepInstructionShower.ShowInstruction();
                // Show wrist up animation
                if (WristAnimationControl != null) WristAnimationControl.ShowWristUpAnimation();
                // lock bucket during instruction
                if (BucketController != null) { BucketController.MovementEnabled = false; }
                break;
            case GameStage.WRIST_UP_GAME:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                Debug.Log("Game4Workflow: entering WRIST_UP_GAME");
                if (Timer != null) { Debug.Log("Game4Workflow: Starting Timer for WRIST_UP_GAME"); Timer.StartTimer(TimerDuration); }
                if (BucketController != null) { BucketController.MovementEnabled = true; }
                if (WristAnimationControl != null) WristAnimationControl.HideAnimations();
                break;
            case GameStage.ROUND_RESULT:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(Score.GetResultForRound());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
                RoundResultShower.Show();
                if (BucketController != null) { BucketController.MovementEnabled = false; }
                if (WristAnimationControl != null) WristAnimationControl.HideAnimations();
                break;
            case GameStage.FINISHED:
                RoundResultShower.Hide();
                Debug.Log("Game Finished");
                if (BucketController != null) { BucketController.MovementEnabled = false; }
                if (WristAnimationControl != null) WristAnimationControl.HideAnimations();
                displayScore();
                break;
            default:
                GameStepInstructionShower.HideInstruction();
                break;
        }
    }

    private void resetScores()
    {
        NumWavings = 0;
        WristAngle = 0;
        MaxWristAngle = -99999;
        MinWristAngle = 99999;
        MaxAngleExceeded = false;
        MinAngleExceeded = false;
    }

    public void onVisibilityEndured()
    {
        switch (CurrentStage)
        {
            case GameStage.WRIST_DOWN_INSTRUCTION:
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            case GameStage.WRIST_UP_INSTRUCTION:
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            default:
                //do nothing
                break;
        }
    }

    public void onCropCut()
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

    public void onNextRoundButtonClicked()
    {
        Debug.Log("Next round button clicked");
        moveToNextStage();
    }
}
