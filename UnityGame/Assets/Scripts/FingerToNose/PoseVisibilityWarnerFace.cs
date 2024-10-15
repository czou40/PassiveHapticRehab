using UnityEngine;
using UnityEngine.UI;
using TMPro;
using UnityEngine.Events;

public class PoseVisibilityWarnerFace : MonoBehaviour
{

    private FaceCaptureDataReceiver faceCaptureDataReceiver;

    [SerializeField] private GameObject warningObject;

    public string WarningMessage = "Please adjust your position so that your face is visible.";

    public bool isInGameScene = false;

    public bool shouldShowWhenPaused = true;

    public bool shouldPauseWhenNotVisible = false;

    public bool shouldShowWarningWhenNotVisible = true;

    public float countDownTimeBeforeGameStart = 5.0f;

    public float countDownTimeBeforeGameResume = 3.0f;

    public UnityEvent onVisibilityLost;
    public UnityEvent onVisibilityGained;
    public UnityEvent onVisibilityCountDownEnd;

    private TextMeshProUGUI warningText;

    private bool canVisibilityLostEventTrigger = true;
    private bool canVisibilityGainedEventTrigger = true;
    private bool canVisibilityCountDownEndEventTrigger = true;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        warningText = warningObject.GetComponentInChildren<TextMeshProUGUI>();
        warningText.text = WarningMessage;
        // faceCaptureDataReceiver = GameManager.Instance.faceCaptureDataReceiver;
    }

    // Update is called once per frame
    void Update()
    {
        // if (GameManager.Instance.gamePaused && !shouldShowWhenPaused)
        // {
        //     HideWarning();
        //     return;
        // }
        // if (faceCaptureDataReceiver.IsFaceVisible)
        // {
            // float visibleTime = faceCaptureDataReceiver.secondsSinceUpperBodyVisible;
            // if (isInGameScene && visibleTime > countDownTimeBeforeGameResume || !isInGameScene && visibleTime > countDownTimeBeforeGameStart)
            // {
            //     HideWarning();
            //     if (canVisibilityCountDownEndEventTrigger)
            //     {
            //         if (shouldPauseWhenNotVisible)
            //         {
            //             GameManager.Instance.ResumeGame();
            //         }
            //         onVisibilityCountDownEnd.Invoke();
            //     }
            //     canVisibilityGainedEventTrigger = false;
            //     canVisibilityCountDownEndEventTrigger = false;
            //     canVisibilityLostEventTrigger = true;
            // }
            // else
            // {
            //     warningText.text = WarningMessage + "\n" + "Game " + (isInGameScene ? "resuming" : "starting") + " in " + (isInGameScene ? countDownTimeBeforeGameResume - visibleTime : countDownTimeBeforeGameStart - visibleTime).ToString("F1") + " seconds.";
            
            //     ShowWarning();
    
            //     if (canVisibilityGainedEventTrigger)
            //     {
            //         onVisibilityGained.Invoke();
            //     }
            //     canVisibilityGainedEventTrigger = false;
            //     canVisibilityCountDownEndEventTrigger = true;
            //     canVisibilityLostEventTrigger = true;
            // }
        // }
        // else
        // {
        //     if (isInGameScene && shouldPauseWhenNotVisible)
        //     {
        //         warningText.text = WarningMessage + "\n" + "Game paused.";
        //     }
        //     else
        //     {
        //         warningText.text = WarningMessage;
        //     }
            
        //     ShowWarning();
            
        //     if (canVisibilityLostEventTrigger)
        //     {
        //         if (shouldPauseWhenNotVisible)
        //         {
        //             GameManager.Instance.PauseGame();
        //         }
        //         onVisibilityLost.Invoke();
        //     }
        //     canVisibilityGainedEventTrigger = true;
        //     canVisibilityCountDownEndEventTrigger = true;
        //     canVisibilityLostEventTrigger = false;
        // }
    }

    private void HideWarning()
    {
        warningObject.SetActive(false);
    }

    private void ShowWarning()
    {
        if (!shouldShowWarningWhenNotVisible)
        {
            warningObject.SetActive(false);
        } else
        {
            warningObject.SetActive(true);
        }
    }

    public void ResetTriggers()
    {
        canVisibilityGainedEventTrigger = true;
        canVisibilityCountDownEndEventTrigger = true;
        canVisibilityLostEventTrigger = true;
    }
}
