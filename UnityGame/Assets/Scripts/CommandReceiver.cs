using UnityEngine;

public class CommandReceiver : MonoBehaviour
{
    void ReceiveCommand(string command)
    {
        Debug.Log("Received command: " + command);
        if (command == "quit")
        {
            Application.Quit();
        }
        else if (command.StartsWith("load")) // e.g. load Scene1
        {
            string[] s = command.Split(' ');
            if (s.Length < 2)
            {
                Debug.LogWarning("Invalid command: " + command);
            }
            else
            {
                string sceneName = s[1];
                UnityEngine.SceneManagement.SceneManager.LoadScene(sceneName);
            }
        }
        else if (command.StartsWith("pauseload") || command.StartsWith("pload")) // e.g. pauseload Scene1
        {
            string[] s = command.Split(' ');
            if (s.Length < 2)
            {
                Debug.LogWarning("Invalid command: " + command);
            }
            else
            {
                string sceneName = s[1];
                GameManager.Instance.PauseGame();
                UnityEngine.SceneManagement.SceneManager.LoadScene(sceneName);
            }
        }
        else if (command == "pause")
        {
            GameManager.Instance.PauseGame();
        }
        else if (command == "resume")
        {
            GameManager.Instance.ResumeGame();
        }
        else
        {
            Debug.LogWarning("Invalid command: " + command);
        }
    }
}
