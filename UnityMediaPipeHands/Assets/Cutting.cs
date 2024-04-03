using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;

public class Cutting : MonoBehaviour
{
    [SerializeField] private DataReceiver dataReceiver;

    public GameObject bottomRightCrop;
    //public GameObject bottomRightCrop;

    //public GameObject bottomRightCrop;
    //public GameObject bottomRightCrop;
    //public GameObject bottomRightCrop;
   // public GameObject bottomRightCrop;

    public GameObject bottomRightCrop4;
    public GameObject bottomRightCrop3;
    public GameObject bottomRightCrop2;


    private int cropDeactivationCount = 0;

    public TextMeshProUGUI counterText;

    HashSet<GameObject> crops;


    // Start is called before the first frame update
    void Start()
    {
        crops = new HashSet<GameObject>();
        counterText.text = "Crop Deactivated: " + cropDeactivationCount.ToString();
    }

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.HasPoseData) {
            if (dataReceiver.getLeftShoulderAngle() > 100 && dataReceiver.getLeftShoulderAngle() < 120){
                // bottomRightCrop.SetActive(false);
                cropDeactivationCount++; // Increment the counter
                Debug.Log(dataReceiver.getLeftShoulderAngle());
                counterText.text = "Crops Cut: " + cropDeactivationCount.ToString();
            }
        }
    }
}
