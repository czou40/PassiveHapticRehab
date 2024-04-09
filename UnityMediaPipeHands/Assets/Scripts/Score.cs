using UnityEngine;
using TMPro;

public class Score : MonoBehaviour
{
    private TMP_Text scoreText;

    void Start()
    {
        scoreText = GetComponent<TMP_Text>();
        GenerateScore();
    }

    void Update()
    {
        // For updating score every frame uncomment following line:
        // GenerateScore();
    }

    void GenerateScore()
    {
        //Change this score assignment to something based on input from game (maybe max shoulder angle reached...)
        int score = Random.Range(0, 1000);
        scoreText.text = "Score: " + score.ToString();
    }
}