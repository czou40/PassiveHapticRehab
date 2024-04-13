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
        canvasGroup = GetComponent<CanvasGroup>();
        if (canvasGroup == null)
        {
            Debug.LogError("PauseMenuToggle script requires a CanvasGroup component attached to the same GameObject.");
        }

        // Initially visible
        ShowMenu();
    }

    // Call this method to hide the menu
    public void HideMenu()
    {
        canvasGroup.alpha = 0f;
        canvasGroup.interactable = false;
        canvasGroup.blocksRaycasts = false;

        // Optionally, disable the GameObject to prevent it from receiving input.
        gameObject.SetActive(false);
        onMenuHidden?.Invoke();

    }

    // Call this method to show the menu
    private void ShowMenu()
    {
        canvasGroup.alpha = 1f;
        canvasGroup.interactable = true;
        canvasGroup.blocksRaycasts = true;
        onMenuShown?.Invoke();

    }
}