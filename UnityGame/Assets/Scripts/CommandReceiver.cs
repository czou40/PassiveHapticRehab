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
        else
        {
            Debug.LogWarning("Invalid command: " + command);
        }
    }
}
