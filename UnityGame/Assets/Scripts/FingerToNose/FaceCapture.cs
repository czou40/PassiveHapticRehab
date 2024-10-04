using UnityEngine;
using UnityEngine.UI;

public class FaceCapture : MonoBehaviour
{
    private WebCamTexture webcamTexture;

    public Button continueButton;
    public RectTransform faceFrame;
    
    private bool isFaceInsideFrame = false;


    void Start()
    {
        WebCamDevice[] devices = WebCamTexture.devices;
        if (devices.Length > 0)
        {
            webcamTexture = new WebCamTexture(devices[0].name);
            GetComponent<RawImage>().texture = webcamTexture;
            webcamTexture.Play();
        }
    }

    void Update()
    {
        if (isFaceInsideFrame)
        {
            continueButton.interactable = true;
        }
    }

    public void FaceDetected()
    {
        isFaceInsideFrame = true;
    }

    public void FaceNotDetected()
    {
        isFaceInsideFrame = false;
        continueButton.interactable = false;
    }

    void OnDestroy()
    {
        if (webcamTexture != null && webcamTexture.isPlaying)
        {
            webcamTexture.Stop();
        }
    }
}
