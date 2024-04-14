using UnityEngine;
using UnityEngine.SceneManagement;

public class Game1Loader : MonoBehaviour
{
    public string gameSceneName = "Instruction1";
    public void LoadGameScene()
    {
        SceneManager.LoadScene(gameSceneName);
    }
}