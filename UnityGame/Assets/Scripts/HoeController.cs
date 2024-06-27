using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using UnityEngine;
using TMPro;

public class HoeController : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;

    public GameObject hoe;

    public GameObject bottomRightCrop;
    public GameObject basket1;
    public GameObject basket2;


    public GameObject crop2;
    public GameObject crop9;
    public GameObject crop8;
    public GameObject crop;
    public GameObject crop11;

    private long lastCutTime = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();
    private bool canCut = false;
    private bool prevCanCut = false;
    private int count = 0;

    public TextMeshProUGUI counterText;



    // Start is called before the first frame update
    void Start()
    {
        counterText.text = "Crops cut: " + count.ToString();
        basket1.SetActive(false);
        basket2.SetActive(false);
    }



    private void  OnTriggerEnter2D(Collider2D collision) {
        Debug.Log("Collision detected");
        if (GameManager.Instance.gamePaused) {
            return;
        }
        long currentTime = System.DateTimeOffset.Now.ToUnixTimeMilliseconds();
        if (currentTime - lastCutTime < 1000) {
            canCut = prevCanCut;
            lastCutTime = currentTime;
        }
        else {
            prevCanCut = canCut;
            lastCutTime = currentTime;
        }

        if (!canCut) {
            return;
        }
        if (collision.gameObject.name == "crop (2)") {
            canCut = false;
            StartCoroutine(HandleCollisionWithPause(crop2));
        }
        if (collision.gameObject.name == "crop (9)") {
            canCut = false;
            StartCoroutine(HandleCollisionWithPause(crop9));
        }
        if (collision.gameObject.name == "crop (8)") {
            canCut = false;
            StartCoroutine(HandleCollisionWithPause(crop8));
        }
        if (collision.gameObject.name == "crop") {
            canCut = false;
            StartCoroutine(HandleCollisionWithPause(crop));
        }
        if (collision.gameObject.name == "crop (11)") {
            canCut = false;
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
        if (GameManager.Instance.gamePaused) {
            return;
        }
        if (count > 0) {
            basket1.SetActive(true);
            basket2.SetActive(true);
            
        }
        if (dataReceiver.isUpperBodyVisible) {
            float angle = dataReceiver.getLeftShoulderExtensionAngle();
            canCut = canCut || angle > 140;
            prevCanCut = prevCanCut || angle > 140;
            hoe.transform.rotation = Quaternion.Euler(0, 0, -angle+90);
        }

        counterText.text = "Crops Cut: " + count.ToString();
    }
}
