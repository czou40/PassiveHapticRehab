using UnityEngine;
using UnityEngine.UI;
using TMPro;
using UnityEngine.Events;
using System.Collections;
using System.Collections.Generic;


[RequireComponent(typeof(CanvasRenderer))]
public class CircleDrawer : Graphic
{
    public float radius = 100f;
    public Color color = Color.white;
    public int segments = 360;

    protected override void OnPopulateMesh(VertexHelper vh)
    {
        vh.Clear();

        Vector2 center = rectTransform.rect.center;
        float angleIncrement = 360f / segments * Mathf.Deg2Rad;

        for (int i = 0; i < segments; i++)
        {
            float angle = i * angleIncrement;
            float nextAngle = (i + 1) * angleIncrement;

            Vector2 point1 = center + new Vector2(Mathf.Cos(angle) * radius, Mathf.Sin(angle) * radius);
            Vector2 point2 = center + new Vector2(Mathf.Cos(nextAngle) * radius, Mathf.Sin(nextAngle) * radius);

            vh.AddVert(center, color, Vector2.zero);
            vh.AddVert(point1, color, Vector2.zero);
            vh.AddVert(point2, color, Vector2.zero);

            int index = i * 3;
            vh.AddTriangle(index, index + 1, index + 2);
        }
    }
}

public class GameStepInstructionShower : MonoBehaviour
{
    [SerializeField] private GameObject Instruction;
    public UnityEvent onInstructionCountdownEnd;

    private GameObject Countdown;
    private GameObject CountdownCircle;

    private TextMeshProUGUI CountdownText;
    private TextMeshProUGUI InstructionText;
    private GameObject ContentHolder;

    public List<GameObject> ContentObjects = new List<GameObject>();

    private int displayedContentIndex = -1;
    
    private float CountdownTime = 5f;

    private float timeRemaining;
    private bool isCountingDown = false;

    private string instructionTextString = "";

    void Start()
    {
        InstructionText = Instruction.transform.Find("InstructionText").GetComponent<TextMeshProUGUI>();
        Countdown = Instruction.transform.Find("Countdown").gameObject;
        CountdownCircle = Countdown.transform.Find("CountdownCircle").gameObject;
        CountdownText = Countdown.transform.Find("CountdownText").GetComponent<TextMeshProUGUI>();
        ContentHolder = Instruction.transform.Find("ContentHolder").gameObject;
        if (CountdownCircle != null)
        {
            RectTransform countdownRect = CountdownCircle.GetComponent<RectTransform>();
            float diameter = countdownRect.rect.width; // Assuming width and height are the same

            CreateCircle("LargerCircle", diameter, new Color(1f, 1f, 1f, 0.5f));
            CreateCircle("SmallerCircle", diameter * 0.75f, new Color(1f, 1f, 1f, 0.8f));
        }
        else
        {
            Debug.LogWarning("CountdownCircle not found. Ensure it is named correctly and is a child of the Instruction object.");
        }

        ContentObjects.ForEach(contentObject => contentObject.SetActive(false));
    }

    void CreateCircle(string name, float diameter, Color color)
    {
        GameObject circle = new GameObject(name);
        circle.transform.SetParent(CountdownCircle.transform, false);
        CircleDrawer circleDrawer = circle.AddComponent<CircleDrawer>();
        circleDrawer.radius = diameter / 2; // Set radius based on diameter
        circleDrawer.color = color;

        RectTransform rectTransform = circle.GetComponent<RectTransform>();
        rectTransform.sizeDelta = new Vector2(diameter, diameter); // Set size based on diameter
    }

    public void SetInstructionText(string text)
    {
        this.instructionTextString = text;
    }

    public void StartCountdown(int countdownTime = -1)
    {
        if (countdownTime > 0)
        {
            CountdownTime = countdownTime;
        }
        timeRemaining = CountdownTime;
        isCountingDown = true;
    }

    public void ShowInstruction()
    {
        Instruction.SetActive(true);
    }

    public void HideInstruction()
    {
        Instruction.SetActive(false);
    }

    public void SetDisplayedContent(int index) 
    {
        if (displayedContentIndex >= 0)
        {
            ContentObjects[displayedContentIndex].SetActive(false);
        }
        if (index < 0 || index >= ContentObjects.Count)
        {
            Debug.LogWarning("Invalid index for content object. Ensure the index is within the range of the ContentObjects list.");
            displayedContentIndex = -1;
            return;
        }
        ContentObjects[index].SetActive(true);
        displayedContentIndex = index;
    }

    public void HideDisplayedContent()
    {
        if (displayedContentIndex >= 0)
        {
            ContentObjects[displayedContentIndex].SetActive(false);
            displayedContentIndex = -1;
        }
    }
        

    void Update()
    {
        bool shouldDisplayCountdownCircle = isCountingDown && displayedContentIndex < 0;
        InstructionText.text = instructionTextString + (isCountingDown && !shouldDisplayCountdownCircle ? "\n"+ Mathf.Ceil(timeRemaining).ToString() : "");
        if (isCountingDown)
        {
            Countdown.SetActive(shouldDisplayCountdownCircle);
            timeRemaining -= Time.deltaTime;
            Debug.Log("Time left: " + timeRemaining);
            if (timeRemaining <= 0)
            {
                isCountingDown = false;
                timeRemaining = 0;
                onInstructionCountdownEnd.Invoke();
            }
            CountdownText.text = Mathf.Ceil(timeRemaining).ToString();
        } else
        {
            Countdown.SetActive(false);
        }
    }
}

