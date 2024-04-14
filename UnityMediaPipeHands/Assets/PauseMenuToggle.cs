using UnityEngine;
using UnityEngine.Events;

[RequireComponent(typeof(CanvasGroup))]
public class PauseMenuToggle : MonoBehaviour
{
    private CanvasGroup canvasGroup;
    public UnityEvent onMenuHidden;
    public UnityEvent onMenuShown;

    private void Awake()
    {
        if (FindObjectsOfType(GetType()).Length > 1)
        {
            Destroy(gameObject); // Destroy this if another exists
        }
        else
        {
            DontDestroyOnLoad(gameObject); // Only don't destroy the first instance
        }
        canvasGroup = GetComponent<CanvasGroup>();
        ShowMenu(); // Initialize as visible
    }

    public void ShowMenu()
    {
        if (canvasGroup.alpha == 1f) // Add this check to prevent recursive loop if already shown
            return;

        canvasGroup.alpha = 1f;
        canvasGroup.interactable = true;
        canvasGroup.blocksRaycasts = true;

        // Only invoke the event if this action is due to an external request,
        // not when it's already being processed.
        // onMenuShown?.Invoke(); // Consider removing or guarding this to prevent recursion
    }

    public void HideMenu()
    {
        if (canvasGroup.alpha == 0f) // Add this check to prevent recursive loop if already hidden
            return;

        canvasGroup.alpha = 0f;
        canvasGroup.interactable = false;
        canvasGroup.blocksRaycasts = false;
        gameObject.SetActive(false);

        // Only invoke the event if this action is due to an external request,
        // not when it's already being processed.
        // onMenuHidden?.Invoke(); // Consider removing or guarding this to prevent recursion
    }
}