using System.Collections;
using System.Collections.Generic;
using Unity.VisualScripting;
using UnityEngine;
using TMPro;

public class HoeController : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;

    [SerializeField] private TextMeshProUGUI text;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.HasPoseData) {
            float angle = dataReceiver.getLeftShoulderExtensionAngle();
            transform.rotation = Quaternion.Euler(0, 0, -angle+90);
            text.text = "Left Shoulder Angle: " + angle;
        }
    }

}
