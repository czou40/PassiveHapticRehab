using UnityEngine;
using UnityEngine.UI;
using Mediapipe.Unity;
using Mediapipe.Unity.FaceDetection;

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

            // Initialize MediaPipe Face Detection Graph
            faceDetectionGraph = new FaceDetectionGraph();
            faceDetectionGraph.StartRun().AssertOk();

             StartCoroutine(ProcessFrame());
        }
    }

    IEnumerator ProcessFrame()
    {
        while (true)
        {
            if (webCamTexture.didUpdateThisFrame)
            {
                // Send the frame to MediaPipe's face detection pipeline
                var image = new Texture2D(webCamTexture.width, webCamTexture.height);
                image.SetPixels(webCamTexture.GetPixels());
                image.Apply();

                using (var input = new ImageFrame(image))
                {
                    // Detect faces in the frame
                    var faceDetections = faceDetectionGraph.ProcessFrame(input);

                    if (faceDetections.Count > 0)
                    {
                        // If a face is detected, check if it's within the frame
                        if (IsFaceWithinFrame(faceDetections[0]))
                        {
                            continueButton.interactable = true;
                        }
                        else
                        {
                            continueButton.interactable = false;
                        }
                    }
                    else
                    {
                        continueButton.interactable = false;
                    }
                }
            }

            yield return null;
        }
    }

    private bool IsFaceWithinFrame(FaceDetection faceDetection)
    {
        // Get the face bounding box
        var faceBox = faceDetection.LocationData.RelativeBoundingBox;

        // Get the frame where the face should fit
        RectTransform frameRectTransform = faceFrame.GetComponent<RectTransform>();

        // Convert face coordinates to the UI coordinate system
        var facePosition = new Vector2(faceBox.Xmin, faceBox.Ymin);
        var faceSize = new Vector2(faceBox.Width, faceBox.Height);

        // Check if the face bounding box fits within the frame
        if (frameRectTransform.rect.Contains(facePosition) && faceSize.magnitude <= frameRectTransform.rect.size.magnitude)
        {
            return true;
        }

        return false;
    }

    // void Update()
    // {
    //     if (isFaceInsideFrame)
    //     {
    //         continueButton.interactable = true;
    //     }
    // }

    // public void FaceDetected()
    // {
    //     isFaceInsideFrame = true;
    // }

    // public void FaceNotDetected()
    // {
    //     isFaceInsideFrame = false;
    //     continueButton.interactable = false;
    // }

    void OnDestroy()
    {
        if (webcamTexture != null && webcamTexture.isPlaying)
        {
            webcamTexture.Stop();
            faceDetectionGraph.StopRun();
        }
    }
}
