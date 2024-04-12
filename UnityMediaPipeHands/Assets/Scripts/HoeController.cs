using UnityEngine.SceneManagement;
using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using UnityEngine;
using TMPro;

public class HoeController : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;

    [SerializeField] private TextMeshProUGUI text;

    public GameObject bottomRightCrop;
    public GameObject basket1;
    public GameObject basket2;


    public GameObject crop2;
    public GameObject crop9;
    public GameObject crop8;
    public GameObject crop;
    public GameObject crop11;



    public int count = 0;

    public TextMeshProUGUI counterText;



    // Start is called before the first frame update
    void Start()
    {
        counterText.text = "Crops cut: " + count.ToString();
        basket1.SetActive(false);
        basket2.SetActive(false);
    }



    private void OnCollisionEnter2D(Collision2D collision){
        Debug.Log(collision.gameObject.name);
        if (collision.gameObject.name == "crop (2)") {
            StartCoroutine(HandleCollisionWithPause(crop2));
        }
        if (collision.gameObject.name == "crop (9)") {
            StartCoroutine(HandleCollisionWithPause(crop9));
        }
        if (collision.gameObject.name == "crop (8)") {
            StartCoroutine(HandleCollisionWithPause(crop8));
        }
        if (collision.gameObject.name == "crop") {
            StartCoroutine(HandleCollisionWithPause(crop));
        }
        if (collision.gameObject.name == "crop (11)") {
            StartCoroutine(HandleCollisionWithPause(crop11));
        }
    }

    private IEnumerator HandleCollisionWithPause(GameObject crop)
{
    // Increment your counter here if needed
    count++;

    // Deactivate the crop
    crop.SetActive(false);

    // Wait for one second
    yield return new WaitForSeconds(2);

    // Reactivate the crop after the pause
    crop.SetActive(true);
}

    // Update is called once per frame
    void Update()
    {
        
        if (count > 0) {
            basket1.SetActive(true);
            basket2.SetActive(true);
            
        }
        if (dataReceiver.HasPoseData) {
            float angle = dataReceiver.getLeftShoulderExtensionAngle();
            transform.rotation = Quaternion.Euler(0, 0, -angle+90);
            text.text = "Left Shoulder Angle: " + angle;
        }

        counterText.text = "Crops Cut: " + count.ToString();
    }


    public void EndGame()
    {
        PlayerPrefs.SetInt("FinalScore", count);
        PlayerPrefs.Save();
        SceneManager.LoadScene("Score1");
    }
}
