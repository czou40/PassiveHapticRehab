using UnityEngine;
using UnityEngine.SceneManagement;

public class SecondScreen : MonoBehaviour
{
    public string gameSceneName = "Game2";
    public void LoadGameScene()
    {
        SceneManager.LoadScene(gameSceneName);
    }
}