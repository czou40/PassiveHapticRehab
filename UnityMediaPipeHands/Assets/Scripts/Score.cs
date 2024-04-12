using UnityEngine;
using TMPro;

public class Score : MonoBehaviour
{
    private TMP_Text scoreText;

    void Start()
    {
        scoreText = GetComponent<TMP_Text>(); // Ensure this component is attached to a UI TextMeshPro element.
        GenerateScore();
    }

    void GenerateScore()
    {
        // Fetch the score stored in PlayerPrefs and set it as the text for the TextMeshPro UI element.
        int score = PlayerPrefs.GetInt("FinalScore", 0); // The second parameter is a default value if "FinalScore" is not found.
        scoreText.text = score.ToString();
    }


}
