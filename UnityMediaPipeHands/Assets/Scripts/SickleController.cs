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

    public GameObject crop2;
    public GameObject crop9;
    public GameObject crop8;

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
            StartCoroutine(HandleCollisionWithPause(crop2));

        }
        if (collision.gameObject.name == "grass6 (1)"){
            StartCoroutine(HandleCollisionWithPause(crop9));
        }
        if (collision.gameObject.name == "grass6"){
            StartCoroutine(HandleCollisionWithPause(crop8));
            crop2.SetActive(true);
            crop8.SetActive(true);
            crop9.SetActive(true);
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
        curr.y = 0.43f;
        curr.x = 5.68f;
        transform.position = curr;
    }

    if (crop.name == "grass6 (1)") {
        Vector3 curr = transform.position;
        curr.y = -1.41f;
        curr.x = 6.83f;
        transform.position = curr;
    }

    if (crop.name == "grass6") {
        crop2.SetActive(true);
        crop8.SetActive(true);
        crop9.SetActive(true);

        Vector3 curr = transform.position;
        curr.y = 2.98f;
        curr.x = 5.37f;
        transform.position = curr;
    }

    isCollisionReady = true;
    

    // Reactivate the crop after the pause
    // crop.SetActive(true);
}

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.HasPoseData) {
            float angle = dataReceiver.getLeftShoulderRotationAngle();
            Quaternion rot = Quaternion.AngleAxis(360-angle, rotationAxis);
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
