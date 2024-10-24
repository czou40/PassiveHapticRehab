using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;

public class Game1Workflow : MonoBehaviour
{
    // Start is called before the first frame update
    private int NumWavings; // Currently unused
    private float Angle;

    private float MaxAngle = -99999;
    private float MinAngle = 99999;
    //minimum and maximum angle needed to reach to increment score
    private float MinAngleThreshold = 50;
    private float MaxAngleThreshold = 100;
    private int PreGameCountdown = 3;
    private int InstructionCountdown = 5;
    private int InstructionCountdownFirstTime = 10;
    private int TimerDuration = 8;
    private bool MinAngleExceeded = false;
    private bool MaxAngleExceeded = false;

    // private ArrayList MinAngles = new ArrayList();
    // private ArrayList MaxAngles = new ArrayList();

    private Game1Score Score;

    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private PoseVisibilityWarner PoseVisibilityWarner;
    private RoundResultShower RoundResultShower;
    private ShoulderAnimationControl ShoulderAnimationControl;
    private Timer Timer;
    private GameStage CurrentStage = GameStage.PRE_GAME;

    private enum GameStage
    {
        PRE_GAME,
        SHOULDER_UP_INSTRUCTION,
        SHOULDER_UP_GAME,

        SHOULDER_DOWN_INSTRUCTION,

        SHOULDER_DOWN_GAME,

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
        PoseVisibilityWarner = GetComponent<PoseVisibilityWarner>();
        RoundResultShower = GetComponent<RoundResultShower>();
        ShoulderAnimationControl = GetComponent<ShoulderAnimationControl>();
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
            Angle = DataReceiver.getLeftShoulderExtensionAngle();

            if (Angle > MaxAngle && CurrentStage == GameStage.SHOULDER_UP_GAME)
            {
                MaxAngle = Angle;
            }

            if (Angle < MinAngle && CurrentStage == GameStage.SHOULDER_DOWN_GAME)
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
                CurrentStage = GameStage.SHOULDER_UP_INSTRUCTION;
                break;
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
                // MinAngles.Add(MinAngle);
                // MaxAngles.Add(MaxAngle);
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
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                resetScores();
                GameStepInstructionShower.SetInstructionText("Attempt " + (CurrentAttempt + 1) + " out of " + MaxAttempts + ". Get ready to start the game!");
                GameStepInstructionShower.ShowInstruction();
                RoundResultShower.Hide();
                GameStepInstructionShower.StartCountdown(PreGameCountdown);
                ShoulderAnimationControl.HideAnimations();
                break;
            case GameStage.SHOULDER_UP_INSTRUCTION:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("First, you need to flex your shoulder as high as you can to gather more power. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(0);
                break;
            case GameStage.SHOULDER_UP_GAME:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                ShoulderAnimationControl.ShowAnimation1();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.SHOULDER_DOWN_INSTRUCTION:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("Great! Now you can extend your shoulder and push back your arm to harvest!");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(1);
                ShoulderAnimationControl.HideAnimations();
                break;
            case GameStage.SHOULDER_DOWN_GAME:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                ShoulderAnimationControl.ShowAnimation2();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.ROUND_RESULT:
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(Score.GetResultForRound());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
                RoundResultShower.Show();
                ShoulderAnimationControl.HideAnimations();
                break;
            case GameStage.FINISHED:
                RoundResultShower.Hide();
                ShoulderAnimationControl.HideAnimations();
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
        NumWavings = 0;
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
            case GameStage.SHOULDER_UP_INSTRUCTION:
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            case GameStage.SHOULDER_DOWN_INSTRUCTION:
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
}
