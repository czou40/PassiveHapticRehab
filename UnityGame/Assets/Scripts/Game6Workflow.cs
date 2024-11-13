using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;
using TMPro;

// Basically copy this file and use our flow
// https://github.com/czou40/PassiveHapticRehab/blob/master/UnityGame/Assets/Scripts/Game1Workflow.cs
public class Game6Workflow : MonoBehaviour
{
    private Game6Score Score;
    public TMP_Text scoreText; // Reference to the on-screen score text
    private int caterpillarsSpawnedThisRound = 0; // Track number of crows spawned in current round
    private int CorrectFingerThisRound = 0;
    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;
    public int currentSpawnIndex;

    //Variables for finger touch detection
    private DataReceiverFingerCoord fingerReceiver; 
    private int lastTouchedFinger = 0;
    //dictionary maps finger touch numbers to name of the coressponding finger
    private Dictionary<int, string> fingerNames = new Dictionary<int, string>()
    {
        {1, "Index"},
        {2, "Middle"},
        {3, "Ring"},
        {4, "Pinky"}
    };

    // Game management variables
    private int TimerDuration = 30; // length of each round

    private int ScoreThisRound = 0;

    private int PreGameCountdown = 3;  // seconds before the game begins
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private Timer Timer;
    private GameStage CurrentStage;
    private RoundResultShower RoundResultShower;
    [SerializeField] private Caterpillar caterpillarSpawner;

    private enum GameStage
    {
        PRE_GAME,
        FINGER_TAP_COORDINATION_GAME,
        ROUND_RESULT,
        FINISHED
    }

    public static Game6Workflow Instance;

    void Awake()
    {
        // Ensure there's only one instance of Game6Workflow
        if (Instance == null)
        {
            Instance = this;
            fingerReceiver = GetComponent<DataReceiverFingerCoord>();
        }
        else
        {
            Destroy(gameObject); // Prevent duplicate instances
        }
    }

    void Start()
    {
        // PRE_GAME - This stage will give an initial few seconds before the game begins, and game timer starts counting down from 30 seconds
        CurrentStage = GameStage.PRE_GAME;
        Score = new Game6Score();
        Score.MarkStartTime();
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        RoundResultShower = GetComponent<RoundResultShower>();
        Timer = GetComponent<Timer>();
        initializeCurrentStage();
    }

    void Update()
    {
        // Check for new finger touch data every frame
        if (CurrentStage != GameStage.PRE_GAME && fingerReceiver != null)
        {
            CheckFingerTouch();
        }
    }

    private void CheckFingerTouch()
    {
        if (fingerReceiver.HasNewData)
        {
            int touchedFinger = fingerReceiver.TouchedFinger;
            
            // Only log if the touched finger has changed
            if (touchedFinger != lastTouchedFinger)
            {
                Debug.Log($"Touched finger: {touchedFinger}");
                Debug.Log($"Spawned at: {caterpillarSpawner.currentSpawnIndex}");

                lastTouchedFinger = touchedFinger;
                string fingerName = fingerNames.ContainsKey(touchedFinger) ? 
                    fingerNames[touchedFinger] : "No finger";

                if (touchedFinger == (caterpillarSpawner.currentSpawnIndex + 1))
                {
                    UpdateScoreUI();
                }
            }
        }
    }

    public void initializeCurrentStage()
    {
        Debug.Log($"Initializing Stage: {CurrentStage}"); // Debug statement for stage initialization
        switch (CurrentStage)
        {
            case GameStage.PRE_GAME:
                Debug.Log("Entering PRE_GAME stage.");
                //GameManager.Instance.PauseGame();
                //resetScores();
                // GameStepInstructionShower.SetInstructionText("Attempt " + (CurrentAttempt + 1) + " out of " + MaxAttempts + ". Get ready to start the game!");
                // GameStepInstructionShower.ShowInstruction();
                // RoundResultShower.Hide();
                //GameStepInstructionShower.StartCountdown(PreGameCountdown);
                StartCoroutine(CountdownToNextStage(PreGameCountdown));
                break;
            case GameStage.FINGER_TAP_COORDINATION_GAME:
                Debug.Log("Entering FINGER_TAP_COORDINATION_GAME stage.");
                //GameManager.Instance.PauseGame();
                //GameStepInstructionShower.HideDisplayedContent();
                //GameStepInstructionShower.HideInstruction();
                Timer.StartTimer(TimerDuration); // Start the timer for 30 seconds
                if (caterpillarSpawner != null) {
                   StartCoroutine(caterpillarSpawner.CaterpillarSpawn());
                } else {
                    Debug.LogWarning("CaterpillarSpawn reference is missing!");
                }
         
                break;
            case GameStage.ROUND_RESULT:
                Debug.Log("Entering ROUND_RESULT stage.");
                // GameManager.Instance.PauseGame();
                Timer.StopTimer();
                // GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText("40");
                // bool isLastAttempt = CurrentAttempt == MaxAttempts;
                // RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
                RoundResultShower.Show();
                break;
            case GameStage.FINISHED:
                Debug.Log("Entering FINISHED stage.");
                RoundResultShower.Hide();
                Debug.Log("Game Finished");
                displayScore();
                break;
            default:
                Debug.LogWarning("Unknown Game Stage!"); // Warning for unexpected stage
                GameStepInstructionShower.HideInstruction();
                break;
        }
    }

    private IEnumerator CountdownToNextStage(int countdownTime)
    {
        int currentTime = countdownTime;
        while (currentTime > 0)
        {
            Debug.Log($"Countdown: {currentTime} seconds remaining...");
            yield return new WaitForSeconds(1f);
            currentTime--;  // Use a separate variable for counting down
        }
        Debug.Log("Countdown complete! Moving to next stage...");
        moveToNextStage();
    }

    private void resetScores()
    {
        ScoreThisRound = 0;
    }

    public void displayScore()
    {
        GameManager.Instance.sendCompoundScore(Score);
    }

    public void moveToNextStage()
    {
        Debug.Log("Prev Stage: " + CurrentStage);
        switch (CurrentStage)
        {
            case GameStage.PRE_GAME:
                CurrentStage = GameStage.FINGER_TAP_COORDINATION_GAME;
                break;
            case GameStage.FINGER_TAP_COORDINATION_GAME:
                CurrentAttempt += 1;
                Score.AddRound(ScoreThisRound);
                CurrentStage = GameStage.ROUND_RESULT;
                break;
            case GameStage.ROUND_RESULT:
                if (CurrentAttempt < MaxAttempts)
                {
                    Debug.Log("Entering the right place");
                    Timer.StopTimer();
                    RoundResultShower.Hide();
                    resetScores();
                    CurrentStage = GameStage.PRE_GAME;
                }
                else
                {
                    Debug.Log("Enterring the wrong place");
                    Score.MarkEndTime();
                    CurrentStage = GameStage.FINISHED;
                }
                break;
            default:
                // Do nothing
                break;
        }
        Debug.Log("Next Stage: " + CurrentStage); // Debug statement for next stage transition

        initializeCurrentStage();
    }

    private void UpdateScoreUI()
    {
        CorrectFingerThisRound += 1; 
        scoreText.text = CorrectFingerThisRound.ToString();
    }
}
