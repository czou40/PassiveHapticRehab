using UnityEngine;
using TMPro;

public class ShoulderAngleDisplay : MonoBehaviour
{
    private DataReceiver dataReceiver;
    [SerializeField] private TextMeshProUGUI angleDisplayText;
    [SerializeField] private GameObject pivotPointObject;

    void Start()
    {
        dataReceiver = GameManager.Instance.DataReceiver;
    }

    void Update()
    {
        if (dataReceiver != null && dataReceiver.isUpperBodyVisible)
        {
            float angle = dataReceiver.getLeftShoulderExtensionAngle();
            angleDisplayText.text = "Shoulder Angle: " + angle.ToString("F2") + "°";
            pivotPointObject.transform.eulerAngles = new Vector3(0,0,-angle-180);
        }
    }
}