using UnityEngine;

public class HandMovementControl : MonoBehaviour
{


    public GameObject InstructionObject1;
    public GameObject InstructionObject2;
    
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        InstructionObject1.SetActive(false);
        InstructionObject2.SetActive(false);
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void ShowInstruction1()
    {
        InstructionObject1.SetActive(true);
        InstructionObject2.SetActive(false);
    }

    public void ShowInstruction2()
    {
        InstructionObject1.SetActive(false);
        InstructionObject2.SetActive(true);
    }

    public void HideInstruction()
    {
        InstructionObject1.SetActive(false);
        InstructionObject2.SetActive(false);
    }
}
