The code base is adapted from [this repo](https://github.com/ganeshsar/UnityPythonMediaPipeHands) by  ganeshar.

Changes made (Chunhao): 

1. Modified the way data is sent to Unity to make it simpler. It now uses UDP. 

2. Cleaned up the python code and allowed it to exit gracefully when ctrl-c is pressed.

3. Decoupled the python code from Unity. Now the python code can run independently of Unity.


# Multithreaded Unity Python MediaPipe Hands
Testing hand tracking inside of Unity using Google MediaPipe Hands Python framework. Webcam readings, piping, and MediaPipe Hands all run on a different thread.

# Instructions
1. Requires Python, Unity Hub, a WebCam, and decently fast CPU.
2. pip install mediapipe
3. Run the Unity project and main.py

# Tips
* You can set the DEBUG flag True in hands.py to visualize what is being seen and how your hands are being interpreted.
* Improve the accuracy of the model by setting MODEL_COMPLEXITY to 1 inside hands.py
* Make sure port 7777 is not used. If it is used, change the port number in both Unity and Python scripts.