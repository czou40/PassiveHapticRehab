using UnityEngine;
using UnityEngine.UI; // Include the UI namespace to work with UI components.
using UnityEngine.SceneManagement;
using System;

    public enum Game
    {
        None = 0,
        Game1 = 1,
        Game2 = 2
    }

public class GameManager : MonoBehaviour
{

    public static GameManager Instance { get; private set; }
    public bool gamePaused { get; private set; } = false;
    public DataReceiver DataReceiver { get; private set; } 

    private int Score = 0;

    private Game CurrentGame = Game.None;

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

            // Subscribe to the sceneLoaded event
            SceneManager.sceneLoaded += onSceneLoaded;

            DataReceiver = gameObject.AddComponent<DataReceiver>();
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

    public void StartGame1Instructions()
    {
        SceneManager.LoadScene("StartScene1"); // Load the game scene
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

    public void ReceiveCommand(string command)
    {
        Debug.Log("Received command: " + command);
        if (command == "quit")
        {
            Exit();
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
                SceneManager.LoadScene(sceneName);
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
                PauseGame();
                SceneManager.LoadScene(sceneName);
            }
        }
        else if (command == "pause")
        {
            PauseGame();
        }
        else if (command == "resume")
        {
            ResumeGame();
        }
        else
        {
            Debug.LogWarning("Invalid command: " + command);
        }
    }

    public void DisplayScore(Game game, int score)
    {
        CurrentGame = game;
        Score = score;
        sendScore(game, score);
        SceneManager.LoadScene("Score1");
    }

    private void sendScore(Game game, int score)
    {
        // Create a JSON object to send the score and game name.
        DataSender.sendStr("sendScore|" + game.ToString() + "|" + score.ToString());
    }

    private void onSceneLoaded(Scene scene, LoadSceneMode mode){
        if (scene.name == "Score1") {
            GameObject scoreText = GameObject.FindWithTag("scoreText");
            if(scoreText == null){
                Debug.Log("cannot find score Text");
            } else {
                Score comp = scoreText.GetComponent<Score>();
                if(comp == null){
                    Debug.Log("cannot find score component");
                } else {
                    comp.displayScore(Score);
                }
            }
        }
    }
}