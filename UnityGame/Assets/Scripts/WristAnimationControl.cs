using UnityEngine;

public class WristAnimationControl : MonoBehaviour
{
    public GameObject WristDownAnimationObject;
    public GameObject WristUpAnimationObject;
    
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        WristDownAnimationObject.SetActive(false);
        WristUpAnimationObject.SetActive(false);
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void ShowWristDownAnimation()
    {
        Debug.Log("WristAnimationControl: ShowWristDownAnimation called");
        WristDownAnimationObject.SetActive(true);
        WristUpAnimationObject.SetActive(false);
    }

    public void ShowWristUpAnimation()
    {
        Debug.Log("WristAnimationControl: ShowWristUpAnimation called");
        WristDownAnimationObject.SetActive(false);
        WristUpAnimationObject.SetActive(true);
    }

    public void HideAnimations()
    {
        Debug.Log("WristAnimationControl: HideAnimations called");
        WristDownAnimationObject.SetActive(false);
        WristUpAnimationObject.SetActive(false);
    }
}