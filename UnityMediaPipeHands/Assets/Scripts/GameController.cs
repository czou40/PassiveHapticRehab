using UnityEngine;
using UnityEngine.UI; // Include the UI namespace to work with UI components.

public class GameController : MonoBehaviour
{
    public bool gameStarted = false; // Boolean flag to check if the game has started.

    void Start()
    {
        PauseGame(); // Call this function at start to pause the game.
    }

    // Function to pause the game.
    void PauseGame()
    {
        Time.timeScale = 0; // This pauses the game.
        // Disable any gameplay mechanics or movement here.
    }

    // Function to start the game.
    public void StartGame()
    {
        gameStarted = true; // Set the flag to true.
        Time.timeScale = 1; // This unpauses the game.
        // Enable your gameplay mechanics or movement here.
    }
}