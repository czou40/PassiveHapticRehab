using UnityEngine;
using TMPro;
using System;
//using static ScoreControl;

public class Score : MonoBehaviour
{
    private TMP_Text scoreText;
    //public ScoreControl scoreControl;

    void Start()
    {


    }
    //GenerateScore();


    void Update()
    {
        // For updating score every frame uncomment following line:
        // GenerateScore();
    }

    // void GenerateScore()
    // {
    //     //Change this score assignment to something based on input from game (maybe max shoulder angle reached...)
    //     //int score = Random.Range(0, 1000);
    //     scoreText.text = scoreControl.score.ToString();

    // }
    [Obsolete("Use displayCompoundScore instead")]
    public void displayScore(int score)
    {
        Debug.Log("displaying score text");

        Debug.Log(score);
        //string test = score.ToString();
        //scoreText.text = score.ToString();
        scoreText = GetComponent<TMP_Text>();
        if (scoreText == null)
        {
            Debug.Log("text is null");
            return;
        }
        scoreText.text = score.ToString();
        //int pscore = Random.Range(0,1000);
        //scoreText.text = pscore.ToString();
        //scoreText.text = Convert.ToString(score);

    }

    public void DisplayCompoundScore(GameScore score)
    {
        Debug.Log("displaying compound score text");
        scoreText = GetComponent<TMP_Text>();
        if (scoreText == null)
        {
            Debug.Log("text is null");
            return;
        }
        scoreText.text = score.ToString();
    }
}