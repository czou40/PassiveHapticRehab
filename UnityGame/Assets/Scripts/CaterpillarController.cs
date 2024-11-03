using UnityEngine;

public class CaterpillarController : MonoBehaviour
{
    public float speed = 0.2f;
    float currSpeed;
    public float slowFactor = 0.5f;
    bool goalReached = false;

    private Rigidbody2D rb2d;
    private Vector3 startPosition;
    public GameObject effect;
    private GameObject[] clones;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        clones = new GameObject[3];
        startPosition = transform.position;
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

        clones[0] = (GameObject)Instantiate(effect, transform.position, Quaternion.identity);
        clones[1] = (GameObject)Instantiate(effect, transform.position, Quaternion.identity);
        clones[2] = (GameObject)Instantiate(effect, transform.position, Quaternion.identity);
    }

    public void Reset()
    {
        Debug.Log("Game 5 reset");
        transform.position = startPosition;
        goalReached = false;
        currSpeed = speed;

        for (int i = 0; i < clones.Length; i++) Destroy(clones[i], 0.0f);

    }
}
