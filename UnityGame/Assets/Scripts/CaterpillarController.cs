using UnityEngine;

public class CaterpillarController : MonoBehaviour
{
    public float speed = 0.5f;
    float currSpeed;
    public float slowFactor = 0.5f;
    bool goalReached = false;

    private Rigidbody2D rb2d;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        currSpeed = speed;
        rb2d = GetComponent<Rigidbody2D>();
    }

    // Update is called once per frame
    void Update()
    {
        if (!goalReached)
        {
            transform.position -= new Vector3(0, currSpeed * Time.deltaTime, 0);
        } 
    }

    public void slowMovement()
    {
        currSpeed = currSpeed - 0.1f;
    }

    void OnTriggerEnter2D(Collider2D col)
    {
        Debug.Log(col.gameObject.name + " : " + gameObject.name + " : " + Time.time);
        goalReached = true;
    }
}
