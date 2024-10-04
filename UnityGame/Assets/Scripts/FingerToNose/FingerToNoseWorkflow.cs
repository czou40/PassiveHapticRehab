using UnityEngine;

public class FingerToNoseWorkflow : MonoBehaviour
{
    private Timer Timer;

    public GameObject IntroductionPanel;
    public GameObject PhoneHorizontalPanel;
    public GameObject FaceWithinFramePanel;
    public GameObject FingerToNoseIntroductionPanel;
    private GameStage CurrentStage = GameStage.INTRODUCTION;

    private FingerToNoseScore Score;

    private PoseVisibilityWarner PoseVisibilityWarner;

    private DataReceiver DataReceiver;

    private int TimerDuration = 30;

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
        initializeCurrentStage();
    }

    void Update()
    {
        
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
                CurrentStage = GameStage.FACE_WITHIN_FRAME;
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
                FingerToNoseIntroductionPanel.SetActive(true);
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
        GameManager.Instance.sendCompoundScore(Score);
    }

    public void onVisibilityLost()
    {

    }

    public void onVisibilityGained()
    {

    }
}
