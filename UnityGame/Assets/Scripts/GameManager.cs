using UnityEngine;
using UnityEngine.UI; // Include the UI namespace to work with UI components.
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using UnityEngine.Events;

public enum Game
{
    None=0,
    Game1=1,
    Game2=2,
    Game3=3
}

public abstract class GameScore
{
    public Game Game = Game.None;

    public int NumRounds = 0;

    public abstract int Score { get; }

    public long StartTime { get; private set; } = -1;

    public long EndTime { get; private set; } = -1;

    public override abstract string ToString();

    public string ToJson()
    {
        return JsonSerializer.ToJson(this);
    }

    public void MarkStartTime() {
        StartTime = DateTimeOffset.Now.ToUnixTimeMilliseconds();
    }

    public void MarkEndTime() {
        EndTime = DateTimeOffset.Now.ToUnixTimeMilliseconds();
    }
}

public class SimpleScore : GameScore
{
    [JsonProperty]
    public override int Score
    {
        get
        {
            return _score;
        }
    }

    private int _score;

    public SimpleScore(Game game, int score)
    {
        Game = game;
        _score = score;
        NumRounds = 1;
    }

    public override string ToString()
    {
        return $"Game: {Game}\nScore: {Score}";
    }
}

public class Game1Score : GameScore
{
    public List<float> MinAngles { get; private set; }

    public List<float> MaxAngles { get; private set; }

    public override int Score
    {
        get
        {
            if (NumRounds == 0)
            {
                return 0;
            }
            else
            {
                float averageMin = 0;
                float averageMax = 0;
                for (int i = 0; i < NumRounds; i++)
                {
                    averageMin += MinAngles[i];
                    averageMax += MaxAngles[i];
                }
                averageMin /= NumRounds;
                averageMax /= NumRounds;
                return (int)(averageMax - averageMin);
            }
        }
    }

    public Game1Score()
    {
        Game = Game.Game1;
        MinAngles = new List<float>();
        MaxAngles = new List<float>();
    }

    public void AddRound(float minAngle, float maxAngle)
    {
        MinAngles.Add(minAngle);
        MaxAngles.Add(maxAngle);
        NumRounds++;
    }

    public override string ToString()
    {
        string s = "Game: " + Game.ToString() + "\n";
        s += "NumRounds: " + NumRounds.ToString() + "\n";
        s += "MinAngles: ";
        foreach (float f in MinAngles)
        {
            s += f.ToString() + ", ";
        }
        s += "\nMaxAngles: ";
        foreach (float f in MaxAngles)
        {
            s += f.ToString() + ", ";
        }
        s += "\nScore: " + Score.ToString();
        return s;
    }

    public string GetResultForRound(int round)
    {
        if (round < 0 || round >= NumRounds)
        {
            throw new ArgumentOutOfRangeException("round", "Round index out of range");
        }
        double min = MinAngles[round];
        double max = MaxAngles[round];
        // one decimal place
        return $"Round {round + 1}\nMin Angle: {min:F1}\nMax Angle: {max:F1}\nScore: {max - min:F1}";
    }

    public string GetResultForRound()
    {
        return GetResultForRound(NumRounds - 1);
    }
}

public class GameManager : MonoBehaviour
{

    public static GameManager Instance { get; private set; }
    public bool gamePaused { get; private set; } = false;
    public DataReceiver DataReceiver { get; private set; }

    private GameScore CurrentScore;

    private Game CurrentGame = Game.None;

    void Awake()
    {
        Debug.Log("GameController Awake!");
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
        SceneManager.LoadScene("Game1"); // Load the game scene
    }

    public void StartGame1Instructions()
    {
        SceneManager.LoadScene("StartScene1"); // Load the game scene
    }

    public void StartGame2()
    {
        SceneManager.LoadScene("Game2"); // Load the game scene
    }

    public void StartGame3()
    {
        SceneManager.LoadScene("Game3"); // Load the game scene
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

    [Obsolete("Use DisplayCompoundScore instead")]
    public void DisplayScore(Game game, int score)
    {
        CurrentGame = game;
        CurrentScore = new SimpleScore(game, score);
        sendScore(game, score);
        SceneManager.LoadScene("Score1");
    }

    public void DisplayCompoundScore(GameScore score)
    {
        CurrentScore = score;
        sendCompoundScore(score);
        SceneManager.LoadScene("Score1");
    }

    public void sendCompoundScoreAndExit(GameScore score)
    {
        CurrentScore = score;
        sendCompoundScore(score);
        Exit();
    }

    [Obsolete("Use DisplayCompoundScore instead")]
    private void sendScore(Game game, int score)
    {
        // Create a JSON object to send the score and game name.
        DataSender.sendStr("sendScore|" + game.ToString() + "|" + score.ToString());
    }

    public void sendCompoundScore(GameScore score)
    {
        CurrentScore = score;
        // Create a JSON object to send the score and game name.
        string json = JsonSerializer.ToJson(score);
        string command = "sendCompoundScore|" + score.Game.ToString() + "|" + json;
        Debug.Log("Sending command: " + command);
        DataSender.sendStr(command);
    }

    private void onSceneLoaded(Scene scene, LoadSceneMode mode)
    {
        if (scene.name == "Score1")
        {
            GameObject scoreText = GameObject.FindWithTag("scoreText");
            if (scoreText == null)
            {
                Debug.Log("cannot find score Text");
            }
            else
            {
                Score comp = scoreText.GetComponent<Score>();
                if (comp == null)
                {
                    Debug.Log("cannot find score component");
                }
                else
                {
                    comp.DisplayCompoundScore(CurrentScore);
                }
            }
        }
    }
}