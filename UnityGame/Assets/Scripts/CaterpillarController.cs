using UnityEngine;

public class CaterpillarController : MonoBehaviour
{
    public float speed = 0.5f;
    float currSpeed;
    public float slowFactor = 20f;
    bool goalReached = false;

    public Vector3 startPosition;
    public GameObject effect;
    public GameObject[] clones; 
    private bool active = false;

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        for (int i = 0; i < clones.Length; i++)
        {
            clones[i].SetActive(false);
        }
        transform.position = startPosition;
        currSpeed = speed; 
        //this.active = true;
    }

    // Update is called once per frame
    void Update()
    {
        //if (!goalReached && active)
        //{
        //    transform.position -= new Vector3(0, currSpeed * Time.deltaTime, 0);
        //} 
    }

    public void setActive(bool active)
    {
        this.active = active;
    }

    public void moveCaterpillar()
    {
        Debug.Log("Mvoe caterpillar called");
        if (active)
        {
            Debug.Log("Move caterpillar");
            transform.position -= new Vector3(0, currSpeed * 10 * Time.deltaTime, 0);
            
        }
    }

    void OnTriggerEnter2D(Collider2D col)
    {
        Debug.Log("Collider: "+ col.gameObject.name + " : " + gameObject.name + " : " + Time.time);
        goalReached = true;
        active = false;

        for (int i = 0; i < clones.Length; i++)
        {
            clones[i].SetActive(true);
            Debug.Log("Set active " + clones[i].name);
        }
        
    }

    public void Reset()
    {
        Debug.Log("Game 5 reset");
        transform.position = startPosition;
        goalReached = false;
        active = false;
        currSpeed = speed;

        for (int i = 0; i < clones.Length; i++)
        {
            clones[i].SetActive(false);
        }

    }
}
