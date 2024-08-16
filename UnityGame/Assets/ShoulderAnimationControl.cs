using UnityEngine;

public class ShoulderAnimationControl : MonoBehaviour
{

    public GameObject AnimationObject1;
    public GameObject AnimationObject2;
    
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        AnimationObject1.SetActive(false);
        AnimationObject2.SetActive(false);
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void ShowAnimation1()
    {
        AnimationObject1.SetActive(true);
        AnimationObject2.SetActive(false);
    }

    public void ShowAnimation2()
    {
        AnimationObject1.SetActive(false);
        AnimationObject2.SetActive(true);
    }

    public void HideAnimations()
    {
        AnimationObject1.SetActive(false);
        AnimationObject2.SetActive(false);
    }
}
