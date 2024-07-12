using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using static Score;
using System.Timers;

public class ScoreControl : MonoBehaviour
{
    // Start is called before the first frame update
    [SerializeField] public int score;
    private float angle;
    //minimum and maximum angle needed to reach to increment score
    private float min_target = 50;
    private float max_target = 100;
    private bool min_exceeded = false;
    private bool max_exceeded = false;

    private int max_retries = 3;
    private int curr_num_tries = 0;
    private DataReceiver dataReceiver;
    void Start()
    {
        SceneManager.sceneLoaded += onSceneLoaded;
        score = 0;
        dataReceiver = GameManager.Instance.DataReceiver;
    }

    // Update is called once per frame
    void Update()
    {
        while (curr_num_tries < max_retries){
            checkScore();

            if (max_exceeded && min_exceeded){
                //condition reached, increment score
                score += 5;
                //reset the exceed flags
                max_exceeded = false;
                min_exceeded = false;
                break;
            }
            curr_num_tries++;
        }

    }

    void checkScore(){
        if (dataReceiver.isUpperBodyVisible) {
            angle = getGameAngle(SceneManager.GetActiveScene().name);
            min_target = getGameMinTarget(SceneManager.GetActiveScene().name);
            
            if (angle > max_target){
                max_exceeded = true;
            } else if (angle < min_target) {
                min_exceeded = true;
            }
        }
    }
    float getGameAngle(string game){
        if (game == "Game1") {
            return dataReceiver.getLeftShoulderExtensionAngle();
        }
        return dataReceiver.getLeftShoulderRotationAngle();
    }

    float getGameMinTarget(string game){
        if (game == "Game1") {
            return min_target;
        }
        return min_target + 20;
    }

    public void displayScore(){
        SceneManager.LoadScene("Score1");
    }

    void onSceneLoaded(Scene scene, LoadSceneMode mode){
        if (scene.name == "Score1"){
            DataSender.sendStr("Score is: " + score.ToString());
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
