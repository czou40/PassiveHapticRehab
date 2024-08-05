using UnityEngine;
using UnityEngine.Events;

/// <summary>
/// This class is intended for testing purposes only!
/// </summary>
public class Cheat : MonoBehaviour
{
    private int clickCount = 0;
    private float lastClickTime = 0f;
    private float clickDelay = 0.2f;

    public UnityEvent onCheatActivated;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    void Update()
    {
        if (Input.GetMouseButtonDown(0)) // Detect left mouse button click
        {
            clickCount++;
            
            if (clickCount == 1)
            {
                lastClickTime = Time.time;
            }
            else
            {
                lastClickTime = Time.time;
                if (clickCount == 5)
                {
                    Debug.Log("you clicked 5 times. Cheat activated");
                    clickCount = 0; // Reset click count after logging
                    onCheatActivated.Invoke();
                }
            }
        }

        // Reset click count if time between clicks exceeds double click delay
        if (Time.time - lastClickTime > clickDelay)
        {
            clickCount = 0;
        }
    }
}
