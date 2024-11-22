using UnityEngine;
using TMPro;

public class Game6Workflow : MonoBehaviour
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

    //private Game3Score Score;
    private int Score = 0;

    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private PoseVisibilityWarner PoseVisibilityWarner;
    private RoundResultShower RoundResultShower;
    private Timer Timer;
    private GameStage CurrentStage = GameStage.PRE_GAME;

    public GameObject flowers;
    public GameObject ScoreText;
    private bool mustStraighten;

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
        //Score = new Game3Score();
        //Score.MarkStartTime();

        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        GameStepInstructionShower.onInstructionCountdownEnd.AddListener(InstructionListener);
        // game 3 requires hand in position
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
    }

    void InstructionListener()
    {
        Debug.Log("Instructions finished");
        CurrentStage = GameStage.CLENCH_GAME;
        GameStepInstructionShower.HideInstruction();

        // Start the timer
        Timer.StartTimer(60);
    }

    void checkScore()
    {
        if (DataReceiver.isUpperBodyVisible)
        {
            Angle = DataReceiver.getLeftAverageFingerExtensionAngle();

            if (CurrentStage == GameStage.CLENCH_GAME && DataReceiver.isOk() && !mustStraighten && flowers.transform.childCount >= 1)
            {
                
                Debug.Log("Fist :)");
                GameObject flower_to_score = flowers.transform.GetChild(0).gameObject;
                Destroy(flower_to_score);
                mustStraighten = true;
                Score += 50;
                updateScoreText();
                if (flowers.transform.childCount == 1)
                {
                    moveToNextStage();
                }
            }

            //if (DataReceiver.isFlat())
            //{
            //    mustStraighten = false;
            //}
        }
    }

    void updateScoreText()
    {
        TextMeshProUGUI text = ScoreText.GetComponent<TextMeshProUGUI>();
        text.text = "Score: " + Score;
        Debug.Log("updating the score to something");
    }


    public void displayScore()
    {
        //GameManager.Instance.sendCompoundScore(Score);
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
                //Score.AddRound(MinAngle, MaxAngle);
                CurrentStage = GameStage.ROUND_RESULT;
                break;
            case GameStage.ROUND_RESULT:
                if (CurrentAttempt < MaxAttempts)
                {
                    CurrentStage = GameStage.PRE_GAME;
                }
                else
                {
                    //Score.MarkEndTime();
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
                GameStepInstructionShower.SetInstructionText("Slowly make the OK symbol in order to collect flowers!");
                GameStepInstructionShower.ShowInstruction();
                RoundResultShower.Hide();
                GameStepInstructionShower.StartCountdown(PreGameCountdown);
                HandMovementControl.HideInstruction();
                Debug.Log("Pre-game End");
                break;
            //case GameStage.UNFURL_INSTRUCTION:
            //    Debug.Log("UNFURL_INSTRUCTION");
            //    GameManager.Instance.PauseGame();
            //    PoseVisibilityWarner.ResetTriggers();
            //    GameStepInstructionShower.SetInstructionText("First, you need to unfurl your fingers to the maximum to harvest more fruits. Ready?");
            //    GameStepInstructionShower.ShowInstruction();
            //    GameStepInstructionShower.SetDisplayedContent(0);
            //    break;
            //case GameStage.UNFURL_GAME:
            //    Debug.Log("UNFURL_GAME");
            //    GameManager.Instance.PauseGame();
            //    PoseVisibilityWarner.ResetTriggers();
            //    GameStepInstructionShower.HideDisplayedContent();
            //    GameStepInstructionShower.HideInstruction();
            //    HandMovementControl.ShowInstruction1();
            //    Timer.StartTimer(TimerDuration);
            //    break;
            //case GameStage.CLENCH_INSTRUCTION:
            //    Debug.Log("CLENCH_INSTRUCTION");
            //    GameManager.Instance.PauseGame();
            //    PoseVisibilityWarner.ResetTriggers();
            //    GameStepInstructionShower.SetInstructionText("Now, you need to clench your fingers tightly to collect more fruits. Ready?");
            //    GameStepInstructionShower.ShowInstruction();
            //    GameStepInstructionShower.SetDisplayedContent(1);
            //    HandMovementControl.HideInstruction();
            //    break;
            //case GameStage.CLENCH_GAME:
            //    Debug.Log("CLENCH_GAME");
            //    GameManager.Instance.PauseGame();
            //    PoseVisibilityWarner.ResetTriggers();
            //    GameStepInstructionShower.HideDisplayedContent();
            //    GameStepInstructionShower.HideInstruction();
            //    HandMovementControl.ShowInstruction2();
            //    Timer.StartTimer(TimerDuration);
            //    break;
            case GameStage.ROUND_RESULT:
                Debug.Log("ROUND_RESULT");
                GameManager.Instance.PauseGame();
                PoseVisibilityWarner.ResetTriggers();
                GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(Score.ToString());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "View Results");
                RoundResultShower.Show();
                HandMovementControl.HideInstruction();
                Timer.StopTimer();
                break;
            //case GameStage.FINISHED:
            //    Debug.Log("FINISHED");
            //    RoundResultShower.Hide();
            //    HandMovementControl.HideInstruction();
            //    Debug.Log("Game Finished");
            //    displayScore();
            //    break;
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
        //while (CurrentAttempt < MaxAttempts)
        //{
        //    CurrentAttempt += 1;
        //    Score.AddRound(0f, 180f);
        //}
        //Score.MarkEndTime();
        //CurrentStage = GameStage.FINISHED;
        //initializeCurrentStage();
    }
}
