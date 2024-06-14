using UnityEngine;
using UnityEngine.UI; // Include the UI namespace to work with UI components.
using UnityEngine.SceneManagement; // Include the SceneManagement namespace to work with scene management.

public class GameManager : MonoBehaviour
{

    public static GameManager Instance { get; private set; }



    public bool gamePaused { get; private set;} = false;

    void Awake()
    {
        Debug.Log("GameController Awake");
        if (Instance == null)
        {
            Instance = this;
            // Set the screen timeout to never sleep.
            Screen.sleepTimeout = SleepTimeout.NeverSleep;
            DontDestroyOnLoad(gameObject);
            Debug.Log("GameController Instance set");
        }
        else
        {
            Debug.Log("No need for GameController Instance");
            Destroy(gameObject);
        }
        
    }

    public void StartGame1()
    {
        SceneManager.LoadScene("Game1") ; // Load the game scene
    }

    public void StartGame2()
    {
        SceneManager.LoadScene("Game2"); // Load the game scene
    }

    // Function to pause the game.
    public void PauseGame()
    {
        Time.timeScale = 0; // This pauses the game.
        gamePaused = true; // Set the flag to true.
        // Disable your gameplay mechanics or movement here.
    }

    // Function to start the game.
    public void ResumeGame()
    {
        gamePaused = false; // Set the flag to false.
        Time.timeScale = 1; // This unpauses the game.
    }

    public void Exit()
    {
        Debug.Log("Exiting game");
        Application.Quit();
    }
}