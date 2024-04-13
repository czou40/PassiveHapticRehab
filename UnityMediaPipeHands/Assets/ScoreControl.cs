using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using static Score;

public class ScoreControl : MonoBehaviour
{
    // Start is called before the first frame update
    [SerializeField] public int score;
    private float angle;
    //minimum and maximum angle needed to reach to increment score
    private const float min_target = 50;
    private const float max_target = 100;
    private bool min_exceeded = false;
    private bool max_exceeded = false;
    [SerializeField] private DataReceiver dataReceiver;
    void Start()
    {
        SceneManager.sceneLoaded += onSceneLoaded;
        score = 10;
    }

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.HasPoseData) {
            if (SceneManager.GetActiveScene().name == "Game1"){
                angle = dataReceiver.getLeftShoulderExtensionAngle();
            } else {
                angle = dataReceiver.getLeftShoulderRotationAngle();
            }
            if (angle > max_target){
                max_exceeded = true;
            } else if (angle < min_target) {
                min_exceeded = true;
            }
        }
        if (max_exceeded&&min_exceeded){
            //condition reached, increment score
            score += 5;
            //reset the exceed flags
            max_exceeded = false;
            min_exceeded = false;
        }
    }

    public void displayScore(){

        SceneManager.LoadScene("Score1");
        



    }

    void onSceneLoaded(Scene scene, LoadSceneMode mode){
        if (scene.name == "Score1"){
            GameObject scoreText = GameObject.FindWithTag("scoreText");
            if(scoreText == null){
                Debug.Log("cannot find score Text");
            } else {
                
                Score comp = scoreText.GetComponent<Score>();
                if(comp == null){
                    Debug.Log("cannot find score component");
                } else {
                    comp.displayScore(score);
                }
                //scoreText.GetComponent<Score>().displayScore(score);
            }
        }

    }
}
