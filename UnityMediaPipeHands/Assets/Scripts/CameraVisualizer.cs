using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class CameraVisualizer : MonoBehaviour
{

    [SerializeField] private DataReceiver dataReceiver;

    private RawImage rawImage;

    private float rawImageSize;

    // Start is called before the first frame update
    void Start()
    {
        rawImage = GetComponent<RawImage>();
        rawImageSize = rawImage.rectTransform.rect.width * rawImage.rectTransform.rect.height;
        AdjustRectTransform();
    }

    void AdjustRectTransform()
    {
        RectTransform rectTransform = rawImage.rectTransform;
        rectTransform.anchorMin = new Vector2(1, 0);
        rectTransform.anchorMax = new Vector2(1, 0);
        rectTransform.pivot = new Vector2(1, 0);
        rectTransform.anchoredPosition = Vector2.zero;
    }

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.hasImageData)
        {
            byte[] data = dataReceiver.ImageData;

            // Convert byte array to Texture2D
            Texture2D tex = new Texture2D(1, 1);
            if (tex.LoadImage(data))  // Loads the image from byte array
            {
                rawImage.texture = tex;
                rawImage.SetNativeSize();

                // Calculate new size maintaining aspect ratio with area = rawImageSize
                float aspectRatio = (float)tex.width / tex.height;
                float newHeight = Mathf.Sqrt(rawImageSize / aspectRatio);
                float newWidth = aspectRatio * newHeight;

                // Apply the calculated size to the RectTransform
                RectTransform rectTransform = rawImage.rectTransform;
                rectTransform.sizeDelta = new Vector2(newWidth, newHeight);
            }
            else
            {
                Debug.LogError("Failed to convert received data to Texture2D.");
            }
        }
    }
}
