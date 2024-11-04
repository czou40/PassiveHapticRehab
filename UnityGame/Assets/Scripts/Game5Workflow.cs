using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;
using TMPro;
using UnityEngine.Events;


public class Game5Workflow : MonoBehaviour
{
    // Start is called before the first frame update
    private int FingerTapCount; // Currently unused
    private float Angle;

    private float MaxAngle = -99999;
    private float MinAngle = 99999;
    // //minimum and maximum angle needed to reach to increment score
    private float MinAngleThreshold = 110.0f;
    private float MaxAngleThreshold = 10.0f;
    private int PreGameCountdown = 3;
    private int InstructionCountdown = 5;
    private int InstructionCountdownFirstTime = 10;
    private int TimerDuration = 10; //TODO: change to 30
    //private bool MinAngleExceeded = false;
    private bool fingerTouching = false;

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

    [SerializeField] private TMP_Text ScoreText;
    [SerializeField] private GameObject caterpillar;
    public UnityEvent gameEvent;

    public List<GameObject> Game5Score = new List<GameObject>();

    private const float MAX_SCALE = 0.9f;
    private const float MIN_SCALE = 0.1f;

    private enum GameStage
    {
        PRE_GAME,

        INDEX_INSTRUCTION,

        INDEX_GAME,

        ROUND_RESULT_INDEX,

        MIDDLE_INSTRUCTION,

        MIDDLE_GAME,

        ROUND_RESULT_MIDDLE,

        RING_INSTRUCTION,

        RING_GAME,

        ROUND_RESULT_RING,

        PINKIE_INSTRUCTION,

        PINKIE_GAME,

        ROUND_RESULT_PINKIE,

        ROUND_RESULT,

        FINISHED
    }

    void Start()
    {
        FingerTapCount = 0;
        CurrentStage = GameStage.PRE_GAME;
        Score = new Game5Score();
        Score.MarkStartTime();
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        // game 5 requires hand in position
        PoseVisibilityWarner = GetComponent<PoseVisibilityWarner>();
        RoundResultShower = GetComponent<RoundResultShower>();
        HandMovementControl = GetComponent<HandMovementControl>();
        Timer = GetComponent<Timer>();
        initializeCurrentStage();
        caterpillar.GetComponent<CaterpillarController>();
    }

    // Update is called once per frame
    void Update()
    {
        checkScore();

        if (fingerTouching)
        {
            //condition reached, increment score
            FingerTapCount += 1;
            updateCaterpillar();
            //reset the exceed flags
            fingerTouching = false;
        }
    }

    void updateCaterpillar()
    {
        float percentSize = 0.0f;
       
        if (CurrentStage == GameStage.INDEX_GAME || CurrentStage == GameStage.MIDDLE_GAME || CurrentStage == GameStage.RING_GAME || CurrentStage == GameStage.PINKIE_GAME)
        {
            caterpillar.GetComponent<CaterpillarController>().slowMovement();
            //gameEvent.Invoke();
            /*
            if (MaxAngle >= MaxAngleThreshold)
            {
                caterpillar.transform.localScale = new Vector3(MAX_SCALE, MAX_SCALE, 1.0f);
                return;
            }
            else if (MaxAngle >= MinAngleThreshold)
            {
                percentSize = (float)(MaxAngle - MinAngleThreshold) / (MaxAngleThreshold - MinAngleThreshold);
            }
            else
            {
                percentSize = 0.0f;
            }
            */
        }

        //float scale = ((MAX_SCALE - MIN_SCALE) * percentSize) + MIN_SCALE;

        //caterpillar.transform.localScale = new Vector3(scale, scale, 1.0f);
    }

    void checkScore()
    {
        if (DataReceiver.isUpperBodyVisible)
        {
            if (CurrentStage == GameStage.INDEX_GAME)
            {
                Angle = DataReceiver.getLeftIndexFingerAngle();
                Debug.Log("got angle");
            } else if (CurrentStage == GameStage.MIDDLE_GAME)
            {
                Angle = DataReceiver.getLeftAverageFingerExtensionAngle();
            } else if (CurrentStage == GameStage.RING_GAME)
            {
                Angle = DataReceiver.getLeftAverageFingerExtensionAngle();
            } else if (CurrentStage == GameStage.PINKIE_GAME)
            {
                Angle = DataReceiver.getLeftAverageFingerExtensionAngle();
            }

            Debug.Log("Angle: " + Angle);

            if (Angle < MaxAngleThreshold)
            {
                fingerTouching = true;
                updateCaterpillar();
            }
        }

        ScoreText.text = string.Format("Score: {0:0.##}", Score.Score);

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
                CurrentStage = GameStage.INDEX_INSTRUCTION;
                break;
            case GameStage.INDEX_INSTRUCTION:
                CurrentStage = GameStage.INDEX_GAME;
                break;
            case GameStage.INDEX_GAME:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.ROUND_RESULT_INDEX;
                break;
            case GameStage.ROUND_RESULT_INDEX:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.MIDDLE_INSTRUCTION;
                break;
            case GameStage.MIDDLE_INSTRUCTION:
                CurrentStage = GameStage.MIDDLE_GAME;
                break;
            case GameStage.MIDDLE_GAME:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.ROUND_RESULT_MIDDLE;
                break;
            case GameStage.ROUND_RESULT_MIDDLE:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.RING_INSTRUCTION;
                break;
            case GameStage.RING_INSTRUCTION:
                CurrentStage = GameStage.RING_GAME;
                break;
            case GameStage.RING_GAME:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.ROUND_RESULT_RING;
                break;
            case GameStage.ROUND_RESULT_RING:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.PINKIE_INSTRUCTION;
                break;
            case GameStage.PINKIE_INSTRUCTION:
                CurrentStage = GameStage.PINKIE_GAME;
                break;
            case GameStage.PINKIE_GAME:
                CurrentAttempt += 1;
                Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.ROUND_RESULT_PINKIE;
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
            case GameStage.INDEX_INSTRUCTION:
                Debug.Log("INDEX_INSTRUCTION");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("You have 30 seconds to tap as quickly as you can. Let's first start with your index finger. Ready?");
                GameStepInstructionShower.ShowInstruction();
                // GameStepInstructionShower.StartCountdown(InstructionCountdown);
                GameStepInstructionShower.SetDisplayedContent(0);
                break;
            case GameStage.INDEX_GAME:
                Debug.Log("INDEX_GAME");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                HandMovementControl.ShowInstruction1();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.MIDDLE_INSTRUCTION:
                Debug.Log("MIDDLE_INSTRUCTION");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("Now, let's use your middle finger to tap with your thumb. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(1);
                HandMovementControl.HideInstruction();
                break;
            case GameStage.MIDDLE_GAME:
                Debug.Log("MIDDLE_GAME");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                HandMovementControl.ShowInstruction2();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.RING_INSTRUCTION:
                Debug.Log("RING_INSTRUCTION");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("It's time to move on to tapping your ring finger to tap with your thumb. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(1);
                HandMovementControl.HideInstruction();
                break;
            case GameStage.RING_GAME:
                Debug.Log("RING_GAME");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideDisplayedContent();
                GameStepInstructionShower.HideInstruction();
                HandMovementControl.ShowInstruction2();
                Timer.StartTimer(TimerDuration);
                break;
            case GameStage.PINKIE_INSTRUCTION:
                Debug.Log("PINKIE_INSTRUCTION");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.SetInstructionText("Finally, let's use your pinkie finger to tap with your thumb. Ready?");
                GameStepInstructionShower.ShowInstruction();
                GameStepInstructionShower.SetDisplayedContent(1);
                HandMovementControl.HideInstruction();
                break;
            case GameStage.PINKIE_GAME:
                Debug.Log("PINKIE_GAME");
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
                // RoundResultShower.SetResultText(Score.GetResultForRound());
                RoundResultShower.SetResultText(Score.GetResultForRound());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
                RoundResultShower.Show();
                HandMovementControl.HideInstruction();
                break;
            case GameStage.FINISHED:
                Debug.Log("FINISHED");
                // RoundResultShower.SetResultText(Score.ToString());
                // RoundResultShower.Show();
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
        FingerTapCount = 0;
        Angle = 0;
        MaxAngle = -99999;
        MinAngle = 99999;
        fingerTouching = false;

        caterpillar.GetComponent<CaterpillarController>().Reset();
    }

    public void onVisibilityEndured()
    {
        switch (CurrentStage)
        {
            case GameStage.INDEX_INSTRUCTION:
                Debug.Log("RESETTING COUNTDOWN - INDEX");
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            case GameStage.MIDDLE_INSTRUCTION:
                Debug.Log("RESETTING COUNTDOWN - MIDDLE");
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            case GameStage.RING_INSTRUCTION:
                Debug.Log("RESETTING COUNTDOWN - RING");
                GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            case GameStage.PINKIE_INSTRUCTION:
                Debug.Log("RESETTING COUNTDOWN - PINKIE");
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
