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

    // Start is called before the first frame update
    void Start()
    {
        direction = transform.position - rotationPoint.transform.position;
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
    }

}
