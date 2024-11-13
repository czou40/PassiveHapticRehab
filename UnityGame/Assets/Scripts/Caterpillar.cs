using UnityEngine;
using System.Collections;

public class Caterpillar : MonoBehaviour
{
    [SerializeField] GameObject[] caterpillarPrefab;
    [SerializeField] Vector3[] spawnPoints = new Vector3[4];

    [SerializeField] float topYPosition = 5f;
    [SerializeField] float minYPosition = -4f;
    [SerializeField] float speed = 0.05f;
    [SerializeField] float spawnDuration = 30f; // Duration to keep spawning

    private bool isSpawning = true;
    public int currentSpawnIndex;
    public bool newSpawn = false;

    void Start()
    {
        
    }

    public IEnumerator CaterpillarSpawn()
    {
        spawnPoints[0] = new Vector3(-4.94f, topYPosition, 0);
        spawnPoints[1] = new Vector3(-1.6f, topYPosition, 0);
        spawnPoints[2] = new Vector3(1.98f, topYPosition, 0);
        spawnPoints[3] = new Vector3(4.76f, topYPosition, 0);
        StartCoroutine(StopSpawningAfterDelay());

        while (isSpawning)
        {
            currentSpawnIndex = Random.Range(0, spawnPoints.Length);
            newSpawn = true;
            Debug.Log($"Spawned at: {currentSpawnIndex}");
            Vector3 spawnPosition = spawnPoints[currentSpawnIndex];

            GameObject caterpillar = Instantiate(
                caterpillarPrefab[Random.Range(0, caterpillarPrefab.Length)],
                spawnPosition,
                Quaternion.identity
            );

            yield return StartCoroutine(WaitForCaterpillarToReachBottom(caterpillar));

            Destroy(caterpillar);
        }
    }

    private IEnumerator WaitForCaterpillarToReachBottom(GameObject caterpillar)
    {
        while (caterpillar.transform.position.y > minYPosition)
        {
            caterpillar.transform.position -= new Vector3(0, speed * Time.deltaTime, 0);
            yield return null;
        }
    }

    public IEnumerator StopSpawningAfterDelay()
    {
        yield return new WaitForSeconds(spawnDuration);
        Debug.Log("Stop spawning after delay");
        isSpawning = false;
    }
}
