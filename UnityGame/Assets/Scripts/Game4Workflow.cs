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

    private bool canClickCrow = false;

    public GameObject crowPrefab;
    private GameObject currentCrow;
    public float crowRespawnDelay = 1f;

    public static Game4Workflow Instance {get; private set;}

    private enum GameStage
    {
        PRE_GAME,
        // FINGER_TO_NOSE_INSTRUCTION, -- not used right now
        FINGER_TO_NOSE_GAME,
        ROUND_RESULT,
        FINISHED
    }

    private void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
        }
        else
        {
            Destroy(gameObject);
        }
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
        if (crowPrefab != null)
        {
            currentCrow = Instantiate(crowPrefab);
            currentCrow.SetActive(false);
        }
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
                // RoundResultShower.Hide();
                //GameStepInstructionShower.StartCountdown(PreGameCountdown);
                StartCoroutine(CountdownToNextStage(PreGameCountdown));
                break;
            case GameStage.FINGER_TO_NOSE_GAME:
                Debug.Log("Entering FINGER_TO_NOSE_GAME stage.");
                //GameManager.Instance.PauseGame();
                //GameStepInstructionShower.HideDisplayedContent();
                //GameStepInstructionShower.HideInstruction();
                Timer.StartTimer(TimerDuration); // Start the timer for 30 seconds
                break;
            case GameStage.ROUND_RESULT:
                Debug.Log("Entering ROUND_RESULT stage.");
                GameManager.Instance.PauseGame();
                GameStepInstructionShower.HideInstruction();
                RoundResultShower.SetResultText(Score.GetResultForRound());
                bool isLastAttempt = CurrentAttempt == MaxAttempts;
                RoundResultShower.SetNextButtonText(isLastAttempt ? "View Results" : "Jump to Round " + (CurrentAttempt + 1));
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
        while (countdownTime > 0)
        {
            Debug.Log($"Countdown: {countdownTime} seconds remaining...");
            yield return new WaitForSeconds(1);
            countdownTime--;
        }
        moveToNextStage();
    }

    private void Update()
    {
        if (DataReceiver.NoseTouchTimestamps.Count > 0)
        {
            canClickCrow = true;
            DataReceiver.NoseTouchTimestamps.Clear();
        }
    }

    public void OnCrowClicked()
    {
        if (canClickCrow)
        {
            CrowClicksThisRound++;
            canClickCrow = false;
            Debug.Log("Crow clicked! Score registered.");

            if (currentCrow != null)
            {
                currentCrow.SetActive(false);
            }

            Invoke(nameof(RespawnCrow), crowRespawnDelay);
        }
    }

    private void RespawnCrow()
    {
        if (currentCrow == null)
        {
            currentCrow = Instantiate(crowPrefab);
        }

        currentCrow.transform.position = GetRandomPosition();
        currentCrow.SetActive(true);
    }

    private Vector3 GetRandomPosition()
    {
        Camera mainCamera = Camera.main;
        float screenWidth = mainCamera.pixelWidth;
        float screenHeight = mainCamera.pixelHeight;

        float randomX = Random.Range(0, screenWidth);
        float randomY = Random.Range(0, screenHeight);

        Vector3 randomScreenPosition = new Vector3(randomX, randomY, mainCamera.nearClipPlane);
        return mainCamera.ScreenToWorldPoint(randomScreenPosition);
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
                    CurrentStage = GameStage.PRE_GAME;
                }
                else
                {
                    Score.MarkEndTime();
                    CurrentStage = GameStage.FINISHED;
                }
                break;
            default:
                // Do nothing
                break;
        }
        Debug.Log("Next Stage: " + CurrentStage); 

        initializeCurrentStage();
    }
}