using UnityEngine;
using UnityEngine.SceneManagement;

public class Game2Loader : MonoBehaviour
{
    public string gameSceneName = "Instruction2";
    public void LoadGameScene()
    {
        SceneManager.LoadScene(gameSceneName);
    }
}