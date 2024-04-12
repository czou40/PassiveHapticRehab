using UnityEngine;
using TMPro;

public class ShoulderAngleDisplay : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;
    [SerializeField] private TextMeshProUGUI angleDisplayText;

    void Update()
    {
        if (dataReceiver != null && dataReceiver.HasPoseData)
        {
            float angle = dataReceiver.getLeftShoulderExtensionAngle();
            angleDisplayText.text = "Shoulder Angle: " + angle.ToString("F2") + "°";
        }
    }
}