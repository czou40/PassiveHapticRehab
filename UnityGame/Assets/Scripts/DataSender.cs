using UnityEngine;

public class DataSender : MonoBehaviour
{
    public static void CallAndroidMethod(string methodName, string str)
    {
        using (var clsUnityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        {
            using (var objActivity = clsUnityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
            {
                objActivity.Call(methodName, str);
            }
        }
    }

    public static void sendStr(string str)
    {
#if !UNITY_EDITOR
#if UNITY_ANDROID
        try
        {
            CallAndroidMethod("receiveCommand", str);
        }
        catch (System.Exception e)
        {
            Debug.Log("Error sending string to Android: " + e.Message);
        }
#endif
#endif
    }
}
