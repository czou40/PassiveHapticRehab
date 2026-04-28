using UnityEngine;

public class flower_effects : MonoBehaviour
{
    public float timer = 0.5f;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        timer -= Time.deltaTime;
        if (timer <= 0.0f)
        {
            gameObject.SetActive(false);
        }
    }
}
