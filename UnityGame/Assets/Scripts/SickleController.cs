using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using UnityEngine;
using TMPro;

public class SickleController : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;

    [SerializeField] private TextMeshProUGUI text;

    [SerializeField] private GameObject rotationPoint;

    private Vector3 rotationAxis = new Vector3(0, 0, 1);

    private Vector3 direction;

    public GameObject grass62;
    public GameObject grass52;
    public GameObject grass42;
    public GameObject grass32;
    public GameObject grass22;
    public GameObject grass12;

    public GameObject bag;

    private int count = 0;

    public TextMeshProUGUI counterText;

    public float speed = 5.0f;
    private bool isCollisionReady = true;
    

    // Start is called before the first frame update
    void Start()
    {
        direction = transform.position - rotationPoint.transform.position;
        counterText.text = "Crops cut: " + count.ToString();
        bag.SetActive(false);
        Vector3 curr = transform.position;
        curr.y = 3.06f;
        transform.position = curr;
    }


    private void OnCollisionEnter2D(Collision2D collision){
        if (!isCollisionReady) return;
        Debug.Log(collision.gameObject.name);
        if (collision.gameObject.name == "grass6 (2)"){
            StartCoroutine(HandleCollisionWithPause(grass62));

        }
        if (collision.gameObject.name == "grass5 (2)"){
            StartCoroutine(HandleCollisionWithPause(grass52));
        }
        if (collision.gameObject.name == "grass4 (2)"){
            StartCoroutine(HandleCollisionWithPause(grass42));
        }
        if (collision.gameObject.name == "grass3 (2)"){
            StartCoroutine(HandleCollisionWithPause(grass32));
        }
        if (collision.gameObject.name == "grass2 (2)"){
            StartCoroutine(HandleCollisionWithPause(grass22));
        }
        if (collision.gameObject.name == "grass1 (2)"){
            StartCoroutine(HandleCollisionWithPause(grass12));
        }
    }

    private IEnumerator HandleCollisionWithPause(GameObject crop)
{
    // Increment your counter here if needed
    isCollisionReady = false;
    count++;
    Debug.Log(crop.name);

    // Deactivate the crop
    crop.SetActive(false);

    // Wait for one second
    yield return new WaitForSeconds(2);

    if (crop.name == "grass6 (2)") {
        Vector3 curr = transform.position;
        curr.x = 3.41f;
        transform.position = curr;
        grass22.SetActive(true);
    }

    if (crop.name == "grass5 (2)") {
        Vector3 curr = transform.position;
        curr.x = 1.61f;
        transform.position = curr;
        grass12.SetActive(true);
    }
    if (crop.name == "grass4 (2)") {
        Vector3 curr = transform.position;
        curr.x = -0.03f;
        transform.position = curr;
        grass62.SetActive(true);
    }
    if (crop.name == "grass3 (2)") {
        Vector3 curr = transform.position;
        curr.x = -2.24f;
        transform.position = curr;
        grass52.SetActive(true);
    }
    if (crop.name == "grass2 (2)") {
        Vector3 curr = transform.position;
        curr.x = -3.55f;
        transform.position = curr;
        grass42.SetActive(true);
    }
    if (crop.name == "grass1 (2)") {
        Vector3 curr = transform.position;
        curr.x = 5.92f;
        transform.position = curr;
        grass32.SetActive(true);
    }

    isCollisionReady = true;
    

    // Reactivate the crop after the pause
    // crop.SetActive(true);
}

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.isUpperBodyVisible) {
            float angle = dataReceiver.getLeftShoulderRotationAngle();
            Quaternion rot = Quaternion.AngleAxis(360-angle, rotationAxis);
            Debug.Log("A: " + rotationPoint.transform.position + rot * direction);
            Debug.Log("B: " + transform.position);
            transform.position = rotationPoint.transform.position + rot * direction;
            transform.localRotation = rot;
            text.text = "Left Shoulder Rotation: " + angle;
        }

        counterText.text = "Crops cut: " + count.ToString();

        if (count > 0) {
            bag.SetActive(true);
        }
    }

}
