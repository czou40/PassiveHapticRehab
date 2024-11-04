using UnityEngine;

public class CaterpillarController : MonoBehaviour
{
    public float speed = 0.2f;
    float currSpeed;
    public float slowFactor = 0.5f;
    bool goalReached = false;

    private Rigidbody2D rb2d;
    public Vector3 startPosition;
    public GameObject effect;
    private GameObject[] clones;
    private bool active = false;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        clones = new GameObject[3];
        transform.position = startPosition;
        currSpeed = speed;
        rb2d = GetComponent<Rigidbody2D>();
    }

    // Update is called once per frame
    void Update()
    {
        if (!goalReached && active)
        {
            transform.position -= new Vector3(0, currSpeed * Time.deltaTime, 0);
        } 
    }

    public void setActive(bool active)
    {
        this.active = active;
    }

    public void slowMovement()
    {
        if (active)
        {
            transform.position += new Vector3(0, currSpeed * Time.deltaTime, 0);
        }
    }

    void OnTriggerEnter2D(Collider2D col)
    {
        Debug.Log("Collider: "+ col.gameObject.name + " : " + gameObject.name + " : " + Time.time);
        goalReached = true;
        active = false;

        clones[0] = (GameObject)Instantiate(effect, transform.position, Quaternion.identity);
        clones[1] = (GameObject)Instantiate(effect, transform.position, Quaternion.identity);
        clones[2] = (GameObject)Instantiate(effect, transform.position, Quaternion.identity);
    }

    public void Reset()
    {
        Debug.Log("Game 5 reset");
        transform.position = startPosition;
        goalReached = false;
        active = false;
        currSpeed = speed;

        //for (int i = 0; i < clones.Length; i++) Destroy(clones[i], 0.0f);

    }
}
