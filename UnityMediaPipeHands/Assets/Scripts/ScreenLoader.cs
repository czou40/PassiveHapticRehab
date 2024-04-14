using UnityEngine;
using UnityEngine.SceneManagement;

public class ScreenLoader : MonoBehaviour
{
    public string gameSceneName = "Game1";
    public void LoadGameScene()
    {
        SceneManager.LoadScene(gameSceneName);
    }
}
