using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using UnityEngine.Events;

public class FingerToNoseWorkflow : MonoBehaviour
{
    private Timer Timer;

    public GameObject IntroductionPanel;
    public GameObject PhoneHorizontalPanel;
    public GameObject FaceWithinFramePanel;
    public GameObject FingerToNoseIntroductionPanel;
    private GameStage CurrentStage = GameStage.INTRODUCTION;

    // private FingerToNoseScore Score;

    private PoseVisibilityWarnerFace poseVisibilityWarnerFace;

    private FaceCaptureDataReceiver faceCaptureDataReceiver;

    private GameStepInstructionShower gameStepInstructionShower;

    private int TimerDuration = 30;

    private int InstructionCountdown = 5;
    private int InstructionCountdownFirstTime = 10;

    public Text countdownText;
    public Text scoreText;
    public GameObject crowPrefab;
    public Transform[] spawnLocations;
    public Animator scarecrowAnimator;

    private int MaxAttempts = 3;
    private int CurrentAttempt = 0;

    private int score = 0;
    private float countdown = 30.0f;
    private bool gameActive = false;

    private enum GameStage
    {
        INTRODUCTION,
        PHONE_HORIZONTAL_INSTRUCTION,
        FACE_WITHIN_FRAME,
        FINGER_TO_NOSE_INSTRUCTION,
        FINGER_TO_NOSE,
        RESULT,
        FINISHED
    }

    void Start() 
    {
        Debug.Log("In start");
        CurrentStage = GameStage.INTRODUCTION;
        // faceCaptureDataReceiver = GameManager.Instance.faceCaptureDataReceiver;
        gameStepInstructionShower = GetComponent<GameStepInstructionShower>();
        poseVisibilityWarnerFace = GetComponent<PoseVisibilityWarnerFace>();
        initializeCurrentStage();
    }

    void Update()
    {
        // if (gameActive)
        // {
        //     UpdateCountdown();
        // }
    }

    IEnumerator StartGameSequence()
    {
        yield return new WaitForSeconds(1f);

        gameActive = true;

        countdownText.gameObject.SetActive(true);
        scoreText.gameObject.SetActive(true);
        score = 0;
        UpdateScoreText();

        if (scarecrowAnimator != null) 
        {
            scarecrowAnimator.SetTrigger("Smile");
        }

        InvokeRepeating("SpawnCrow", 1.0f, UnityEngine.Random.Range(1.0f, 3.0f));

    }

    private void UpdateCountdown()
    {
        countdown -= Time.deltaTime;
        countdownText.text = "Time: " + Mathf.CeilToInt(countdown).ToString();

        if (countdown <= 0) 
        {
            EndGame();
        }
    }

    private void SpawnCrow()
    {
        if (spawnLocations.Length > 0 && crowPrefab != null)
        {
            int randomIndex = UnityEngine.Random.Range(0, spawnLocations.Length);
            Instantiate(crowPrefab, spawnLocations[randomIndex].position, Quaternion.identity);
        }
    }

    private void EndGame()
    {
        gameActive = false;
        CancelInvoke("SpawnCrow");

        countdownText.gameObject.SetActive(false);
        scoreText.gameObject.SetActive(false);

        Debug.Log("Game Over! Final Score: " + score);
    }

    public void AddScore()
    {
        if (gameActive)
        {
            score++;
            UpdateScoreText();
        }
    }

    private void UpdateScoreText()
    {
        if (scoreText != null)
        {
            scoreText.text = "Score: " + score.ToString();
        }
    }


    public void moveToNextStage()
    {
        Debug.Log("Next button clicked");
        switch (CurrentStage)
        {
            case GameStage.INTRODUCTION:
                CurrentStage = GameStage.PHONE_HORIZONTAL_INSTRUCTION;
                break;
            case GameStage.PHONE_HORIZONTAL_INSTRUCTION:
                CurrentStage = GameStage.FINGER_TO_NOSE_INSTRUCTION;
                break;
            case GameStage.FACE_WITHIN_FRAME:
                CurrentStage = GameStage.FINGER_TO_NOSE_INSTRUCTION;
                break;
            case GameStage.FINGER_TO_NOSE_INSTRUCTION:
                CurrentStage = GameStage.FINGER_TO_NOSE;
                break;
            case GameStage.FINGER_TO_NOSE:
                CurrentStage = GameStage.RESULT;
                break;
            default:
                break;
        }

        initializeCurrentStage();
    }

    public void initializeCurrentStage()
    {
        DeactivateAllPanels();

        switch (CurrentStage)
        {
            case GameStage.INTRODUCTION:
                IntroductionPanel.SetActive(true);
                // GameManager.Instance.PauseGame();
                resetScores();
                break;

            case GameStage.PHONE_HORIZONTAL_INSTRUCTION:
                
                PhoneHorizontalPanel.SetActive(true);
                // GameManager.Instance.PauseGame();
                break;

            case GameStage.FACE_WITHIN_FRAME:
                FaceWithinFramePanel.SetActive(true);
                // GameManager.Instance.PauseGame();
                break;

            case GameStage.FINGER_TO_NOSE_INSTRUCTION:
                // if (countdownText != null) countdownText.gameObject.SetActive(false);
                // if (scoreText != null) scoreText.gameObject.SetActive(false);
                FingerToNoseIntroductionPanel.SetActive(true);
                // StartCoroutine(StartGameSequence());
                // GameManager.Instance.PauseGame();
                break;

            case GameStage.FINGER_TO_NOSE:
                // GameManager.Instance.PauseGame();
                Timer.StartTimer(TimerDuration);
                break;

            case GameStage.RESULT:
                // GameManager.Instance.PauseGame();
                displayScore();
                break;

            case GameStage.FINISHED:
                Debug.Log("Game Finished");
                displayScore();
                break;

            default:
                break;
        }
    }

    private void DeactivateAllPanels()
    {
        IntroductionPanel.SetActive(false);
        PhoneHorizontalPanel.SetActive(false);
        FaceWithinFramePanel.SetActive(false);
        FingerToNoseIntroductionPanel.SetActive(false);
    }

    private void resetScores()
    {
        
    }

    public void displayScore()
    {
        // GameManager.Instance.sendCompoundScore(Score);
    }

    public void onVisibilityLost()
    {

    }

    public void onVisibilityGained()
    {

    }

    public void onVisibilityEndured()
    {
        switch (CurrentStage)
        {
            case GameStage.PHONE_HORIZONTAL_INSTRUCTION:
                // GameStepInstructionShower.StartCountdown(CurrentAttempt == 0 ? InstructionCountdownFirstTime : InstructionCountdown);
                break;
            default:
                break;
        }
    }
}
