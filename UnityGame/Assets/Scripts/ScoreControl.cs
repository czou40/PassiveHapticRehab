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
                angle = pollAverageAngle(100, 5000);
            } else {
                angle = dataReceiver.getLeftShoulderRotationAngle();
                min_target = 70;//make target more lenient for game 2 for better results
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
    float pollAverageAngle(interval int, duration int){
        Timer timer = new Timer(interval);
        int elapsedCount = 0;
        float anglesSum = 0.0;
        int anglesCount = 0;
        timer.Elapsed += (source, e) =>
        {
            anglesSum += dataReceiver.getLeftShoulderExtensionAngle();
            anglesCount += 1;
            elapsedCount += interval;

            if (elapsedCount >= duration)
            {
                timer.Stop();
                timer.Dispose();
            }
        };

        timer.Start();

        System.Threading.Thread.Sleep(duration + interval);

        float avgAngle = anglesSum / (float)anglesCount;

        return avgAngle;

    }
}
