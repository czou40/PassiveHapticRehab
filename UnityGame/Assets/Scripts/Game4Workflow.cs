using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Timers;
using JetBrains.Annotations;
using TMPro;

// Basically copy this file and use our flow
// https://github.com/czou40/PassiveHapticRehab/blob/master/UnityGame/Assets/Scripts/Game1Workflow.cs
public class Game4Workflow : MonoBehaviour
{

    // Crow Spawning variables
    public GameObject CrowPrefab; // Reference to the crow prefab
    public int NumberOfCrowsPerRound = 5; // Number of crows to spawn each round
    public Vector2 SpawnAreaMin; // Bottom-left corner of the spawn area
    public Vector2 SpawnAreaMax; // Top-right corner of the spawn area
    public float SpawnInterval = 2.0f; // Time interval (in seconds) between crow spawns
    private Coroutine crowSpawnCoroutine; // Reference to the crow-spawning coroutine
    private int crowsSpawnedThisRound = 0; // Track number of crows spawned in current round


    // Scoring variables
    private Game4Score Score;
    public TMP_Text scoreText;
    private int CrowClicksThisRound = 0;
    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;


    // Game management variables
    private int TimerDuration = 30; // length of each round
    private int PreGameCountdown = 3;  // seconds before the game begins
    private DataReceiver DataReceiver;
    private GameStepInstructionShower GameStepInstructionShower;
    private Timer Timer;
    private GameStage CurrentStage;
    public RoundResultShower RoundResultShower;

    private enum GameStage
    {
        PRE_GAME,
        // FINGER_TO_NOSE_INSTRUCTION, -- not used right now
        FINGER_TO_NOSE_GAME,
        ROUND_RESULT,
        FINISHED
    }

    public static Game4Workflow Instance;

    void Awake()
    {
        // Ensure there's only one instance of Game4Workflow
        if (Instance == null)
        {
            Instance = this;
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
        Score = new Game4Score();
        Score.MarkStartTime();
        UpdateScoreUI();
        DataReceiver = GameManager.Instance.DataReceiver;
        GameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        RoundResultShower = GetComponent<RoundResultShower>();
        Timer = GetComponent<Timer>();
        initializeCurrentStage();
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
                //RoundResultShower.Hide();
                StartCoroutine(CountdownToNextStage(PreGameCountdown));
                break;
            case GameStage.FINGER_TO_NOSE_GAME:
                Debug.Log("Entering FINGER_TO_NOSE_GAME stage.");
                //GameManager.Instance.PauseGame();
                //GameStepInstructionShower.HideDisplayedContent();
                //GameStepInstructionShower.HideInstruction();
                Timer.StartTimer(TimerDuration); // Start the timer for 30 seconds
                StartCrowSpawning();
                //RoundResultShower.Hide();
                break;
            case GameStage.ROUND_RESULT:
                Debug.Log("Entering ROUND_RESULT stage.");
                StopCrowSpawning();
                Timer.StopTimer();

                //GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(CrowClicksThisRound.ToString());
                //bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.Show();
                break;
            case GameStage.FINISHED:
                Debug.Log("Entering FINISHED stage.");
                StopCrowSpawning();
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
        while (countdownTime > 0)
        {
            Debug.Log($"Countdown: {countdownTime} seconds remaining...");
            yield return new WaitForSeconds(1);
            countdownTime--;
        }
        moveToNextStage(); // Move to the next stage after countdown
    }

    private void resetScores()
    {
        CrowClicksThisRound = 0;
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
                CurrentStage = GameStage.FINGER_TO_NOSE_GAME;
                break;
            case GameStage.FINGER_TO_NOSE_GAME:
                CurrentAttempt += 1;
                Score.AddRound(CrowClicksThisRound);
                CurrentStage = GameStage.ROUND_RESULT;
                break;
            case GameStage.ROUND_RESULT:
                if (CurrentAttempt < MaxAttempts)
                {
                    Debug.Log("Entering the right place");
                    Timer.StopTimer();
                    RoundResultShower.Hide();
                    resetScores();
                    UpdateScoreUI();
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

    private void StartCrowSpawning()
    {
        crowsSpawnedThisRound = 0; // Reset crow count for this round
        crowSpawnCoroutine = StartCoroutine(SpawnCrowsOverTime());
    }

    private void StopCrowSpawning()
    {
        if (crowSpawnCoroutine != null)
        {
            StopCoroutine(crowSpawnCoroutine);
            crowSpawnCoroutine = null;
        }
    }

    private IEnumerator SpawnCrowsOverTime()
    {
        while (crowsSpawnedThisRound < NumberOfCrowsPerRound)
        {
            SpawnCrow();
            crowsSpawnedThisRound++;
            yield return new WaitForSeconds(SpawnInterval);
        }
    }

    private void SpawnCrow()
    {
        Vector2 randomPosition = new Vector2(
            Random.Range(SpawnAreaMin.x, SpawnAreaMax.x),
            Random.Range(SpawnAreaMin.y, SpawnAreaMax.y)
        );
        Instantiate(CrowPrefab, randomPosition, Quaternion.identity);
    }

    private void UpdateScoreUI()
    {
        scoreText.text = CrowClicksThisRound.ToString();
    }

    public void CrowTapped()
    {
        CrowClicksThisRound++;
        Debug.Log("Crow clicked"); // Debug statement for next stage transition
        Debug.Log("New total is: " + CrowClicksThisRound);
        UpdateScoreUI(); // Update the score on the UI
    }

}
