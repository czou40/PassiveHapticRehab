This project contains all the code for the passive haptic rehab (PHR) project. It contains a native Android app and a Unity project. The project uses MediaPipe to detect hand and body poses.

# Unity Project

## Instructions
1. Requires Python, Unity (2022.3.27 LTS), a WebCam, and decently fast CPU.
2. pip install mediapipe==0.10.9 (we recommend creating a virtual environment and installing mediapipe in it, using either the native venv, conda, or poetry.)
3. Run the Unity project and mediapipe_hand_tracking_main.py

## Tips
* You can set the DEBUG flag True in hands.py to visualize what is being seen and how your hands are being interpreted.
* Improve the accuracy of the model by setting MODEL_COMPLEXITY to 1 inside hands.py
* Make sure port 7777 is not used. If it is used, change the port number in both Unity and Python scripts.

## Importing Figma Designs to Unity

The design for the Unity project can be found [here](https://www.figma.com/proto/OEFhAYB7VHAqsMBqTBMbz2/%E2%9D%A4%EF%B8%8F-%F0%9F%A7%A4-VTS-Gloves-Final-Design?type=design&node-id=835-483171&t=UpTGEKaSnHCwuobp-1&scaling=scale-down&page-id=683%3A8318&starting-point-node-id=741%3A8569&show-proto-sidebar=1)

Unfortunately, there are no good ways to import Figma designs. I tried unity figma bridge, which is somewhat useful for UI but not for game elements. Also the imported UI is blurry.

To import a design, you need to select elements, copy as svg, save svg, use the Assets/svg2png.py script to convert svg to png, and then import the assets to unity. To run the script, you need to install cairosvg and pillow in your python environment.

## Exporting the Unity Project to Android (Important! Otherwise, the Android app will not work)

TODO: Add instructions on how to export the Unity project to Android.

## Future Work
* Create scripts to control a rigged 3d hand model for better visualization.
* Investigate the Mediapipe Plugin for Unity for better performance.
* Connect game result data to clinically relevant metrics such as passive range of motion, Fugl-Meyer Assessment, etc.

# Android App

The Android app contains both the legacy tests (finger tracking test, tactile sensation test, force sensing test, Modified Ashworth Test) and the new gamified tests which integrate the Unity project. 

The legacy tests mainly use built-in Android sensors (accelerometer, gyroscope, and magnetometer) to measure the user's hand movements, but they are not very easy to conduct. The finger tracking test uses the MediaPipe hand tracking model to detect finger poses, which inspired the new gamified tests.

The new gamified tests uses a foreground service that runs the MediaPipe hand tracking model to detect hand poses. The app sends the hand poses to the Unity Activity using UDP sockets. The Unity game will be displayed on the screen, and the user can interact with the game using their hands.

## Instructions

Carefully follow the instructions to export the Unity project to Android and run the Android app in the Android Studio. No other setup is required.

## Future Work

* Add more games to the app.
* Use a custom Mediapipe Graph to speed up the holistic tracking.

# Other Miscellaneous Files

There is a web app written in React and Next.js to allow for remote control of the Android app. This allows a clinician or caretaker to help the user navigate the app. For the front end, run npm install and npm run dev to start the web app. For the backend, install the dependencies in requirements.txt and run python app.py. The web app uses Socket.IO to communicate with the Android app. A docker-compose file is provided to run the backend in a container.