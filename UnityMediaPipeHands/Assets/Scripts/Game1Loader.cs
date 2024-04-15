using UnityEngine;
using UnityEngine.SceneManagement;

public class Game1Loader : MonoBehaviour
{
    public string gameSceneName = "StartScene";
    public void LoadGameScene()
    {
        SceneManager.LoadScene(gameSceneName);
    }
}