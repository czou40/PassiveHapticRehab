using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class sicklePivotPoint : MonoBehaviour
{
    // Start is called before the first frame update

   private DataReceiver dataReceiver;
    //[SerializeField] private GameObject pivotPoint;
    // Start is called before the first frame update
    void Start()
    {
        dataReceiver = GameManager.Instance.DataReceiver;
    }

    // Update is called once per frame
    void Update()
    {
        if (dataReceiver.isUpperBodyVisible) {
            float angle = dataReceiver.getLeftShoulderRotationAngle();
            //transform.rotation = Quaternion.Euler(0, 0, angle-90);//range: 0 to counterclockwise 180 degrees 
            //transform.RotateAround(pivotPoint.transform.position, Vector3.forward, angle-90);
            transform.eulerAngles = new Vector3(0,0,-angle-90);
        }
    }
}
