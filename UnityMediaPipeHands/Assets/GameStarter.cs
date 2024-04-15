using UnityEngine;
using UnityEngine.SceneManagement;

public class GameStarter : MonoBehaviour
{
    public PauseMenuToggle pauseMenu; // Reference to the PauseMenuToggle script

    public void StartGame1()
    {

        if (pauseMenu != null)
        {
            pauseMenu.HideMenu(); // Hide the panel
        }
        else
        {
            Debug.LogError("Reference to PauseMenuToggle is not set in the GameStarter script.");
        }

        SceneManager.LoadScene("Game1"); // Load the game scene
    }

    public void StartGame2()
    {

        if (pauseMenu != null)
        {
            pauseMenu.HideMenu(); // Hide the panel
        }
        else
        {
            Debug.LogError("Reference to PauseMenuToggle is not set in the GameStarter script.");
        }

        SceneManager.LoadScene("Game2"); // Load the game scene
    }
}